package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBannerItemRequest;
import com.sep490.vtuber_fanhub.dto.requests.GachaBannerItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.BannerItemResponse;
import com.sep490.vtuber_fanhub.dto.responses.GachaResultResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Banner;
import com.sep490.vtuber_fanhub.models.BannerItem;
import com.sep490.vtuber_fanhub.models.Item;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserItem;
import com.sep490.vtuber_fanhub.repositories.BannerItemRepository;
import com.sep490.vtuber_fanhub.repositories.BannerRepository;
import com.sep490.vtuber_fanhub.repositories.ItemRepository;
import com.sep490.vtuber_fanhub.repositories.UserItemRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerItemServiceImpl implements BannerItemService {

    private final BannerItemRepository bannerItemRepository;

    private final BannerRepository bannerRepository;

    private final ItemRepository itemRepository;

    private final UserItemRepository userItemRepository;

    private final UserRepository userRepository;

    private final AuthService authService;

    private final CloudinaryService cloudinaryService;

    private static final Random RANDOM = new Random();

    @Override
    @Transactional
    public String createBannerItem(CreateBannerItemRequest request, MultipartFile image) {
        Banner banner = bannerRepository.findById(request.getBannerId())
                .orElseThrow(() -> new NotFoundException("Banner not found"));

        Item item = null;

        if ("GOOD_LUCK".equalsIgnoreCase(request.getType())) {
        } else if (request.getItemId() != null) {
            item = itemRepository.findById(request.getItemId())
                    .orElseThrow(() -> new NotFoundException("Item not found"));
        } else {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                try {
                    imageUrl = cloudinaryService.uploadFile(image);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image", e);
                }
            }

            item = new Item();
            item.setItemName(request.getItemName());
            item.setDescription(request.getDescription());
            item.setImageUrl(imageUrl);
            item.setCategory(request.getCategory());
            itemRepository.save(item);
        }

        BannerItem bannerItem = new BannerItem();
        bannerItem.setBanner(banner);
        bannerItem.setItem(item); // Will be null for GOOD_LUCK
        bannerItem.setMultiplier(request.getMultiplier());
        bannerItem.setType(request.getType());

        bannerItemRepository.save(bannerItem);

        return "Created banner item successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerItemResponse> getBannerItemsByBannerId(Long bannerId, int pageNo, int pageSize, String sortBy) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        Page<BannerItem> pagedBannerItems = bannerItemRepository.findByBannerId(bannerId, paging);

        if (pagedBannerItems.isEmpty()) {
            return List.of();
        }

        return pagedBannerItems.getContent().stream()
                .map(this::convertToResponse)
                .toList();
    }

    private BannerItemResponse convertToResponse(BannerItem bannerItem) {
        BannerItemResponse response = new BannerItemResponse();
        response.setBannerItemId(bannerItem.getId());
        response.setBannerId(bannerItem.getBanner().getId());
        
        if (bannerItem.getItem() != null) {
            response.setItemId(bannerItem.getItem().getId());
            response.setItemName(bannerItem.getItem().getItemName());
            response.setDescription(bannerItem.getItem().getDescription());
            response.setImageUrl(bannerItem.getItem().getImageUrl());
            response.setCategory(bannerItem.getItem().getCategory());
        } else {
            response.setItemId(null);
            response.setItemName("Good luck next time");
            response.setDescription(null);
            response.setImageUrl(null);
            response.setCategory(null);
        }
        
        response.setMultiplier(bannerItem.getMultiplier());
        response.setType(bannerItem.getType());
        return response;
    }

    @Override
    @Transactional
    public GachaResultResponse gachaBannerItem(GachaBannerItemRequest request, HttpServletRequest httpRequest) {
        User user = authService.getUserFromToken(httpRequest);

        Banner banner = bannerRepository.findById(request.getBannerId())
                .orElseThrow(() -> new NotFoundException("Banner not found"));

        // Check if banner is active
        Instant now = Instant.now();
        if (now.isBefore(banner.getStartTime()) || now.isAfter(banner.getEndTime())) {
            throw new IllegalStateException("Banner is not currently active");
        }

        // Determine gacha cost (default to 5 if not set)
        int gachaCost = banner.getGachaCost() != null ? banner.getGachaCost() : 5;

        // Check user points
        Long userPoints = user.getPoints() != null ? user.getPoints() : 0L;
        if (userPoints < gachaCost) {
            throw new IllegalStateException("Insufficient points. Required: " + gachaCost + ", Available: " + userPoints);
        }

        // Deduct points
        user.setPoints(userPoints - gachaCost);
        userRepository.save(user);

        // Get all banner items for this banner
        List<BannerItem> bannerItems = bannerItemRepository.findByBannerId(banner.getId(), Pageable.unpaged()).getContent();

        if (bannerItems.isEmpty()) {
            throw new NotFoundException("No items available in this banner");
        }

        // Perform weighted random selection based on multiplier
        BannerItem selectedBannerItem = performWeightedRandomSelection(bannerItems);

        // If type is GOOD_LUCK, don't save to UserItem
        if ("GOOD_LUCK".equalsIgnoreCase(selectedBannerItem.getType())) {
            log.info("User {} performed gacha on banner {} and got GOOD_LUCK for {} points",
                    user.getId(), banner.getId(), gachaCost);

            return convertToGachaResponseForGoodLuck(selectedBannerItem, gachaCost, user.getId());
        }

        // For MAIN_REWARD type, check if user already owns this item
        Item selectedItem = selectedBannerItem.getItem();
        List<Item> ownedItems = userItemRepository.findOwnedItemsByUserAndItems(user, List.of(selectedItem));

        if (!ownedItems.isEmpty()) {
            // User already has this MAIN_REWARD item, refund half the cost (floored)
            int pointsRefunded = (int) Math.floor(gachaCost / 2.0);
            user.setPoints(user.getPoints() + pointsRefunded);
            userRepository.save(user);

            log.info("User {} already owned item {} from banner {}, refunded {} points",
                    user.getId(), selectedItem.getId(), banner.getId(), pointsRefunded);

            return convertToGachaResponseForDuplicateReward(selectedBannerItem, gachaCost, pointsRefunded, user.getId());
        }

        // User doesn't own this item yet, create UserItem entry
        UserItem userItem = new UserItem();
        userItem.setUser(user);
        userItem.setItem(selectedItem);
        userItem.setPurchasedAt(Instant.now());
        userItemRepository.save(userItem);

        log.info("User {} performed gacha on banner {} and got item {} for {} points",
                user.getId(), banner.getId(), selectedItem.getId(), gachaCost);

        return convertToGachaResponse(userItem, selectedBannerItem, gachaCost);
    }

    private BannerItem performWeightedRandomSelection(List<BannerItem> bannerItems) {
        // Calculate total weight
        int totalWeight = bannerItems.stream()
                .mapToInt(BannerItem::getMultiplier)
                .sum();

        // Generate random number
        int randomValue = RANDOM.nextInt(totalWeight);

        // Select item based on weight
        int currentWeight = 0;
        for (BannerItem bannerItem : bannerItems) {
            currentWeight += bannerItem.getMultiplier();
            if (randomValue < currentWeight) {
                return bannerItem;
            }
        }

        // Fallback (should never happen)
        return bannerItems.get(bannerItems.size() - 1);
    }

    private GachaResultResponse convertToGachaResponse(UserItem userItem, BannerItem bannerItem, int gachaCost) {
        GachaResultResponse response = new GachaResultResponse();
        response.setUserItemId(userItem.getId());
        response.setUserId(userItem.getUser().getId());
        response.setItemId(userItem.getItem().getId());
        response.setItemName(userItem.getItem().getItemName());
        response.setImageUrl(userItem.getItem().getImageUrl());
        response.setMultiplier(bannerItem.getMultiplier());
        response.setType(bannerItem.getType());
        response.setCost(gachaCost);
        response.setPointsRefunded(0);
        response.setObtainedAt(userItem.getPurchasedAt());
        return response;
    }

    private GachaResultResponse convertToGachaResponseForGoodLuck(BannerItem bannerItem, int gachaCost, Long userId) {
        GachaResultResponse response = new GachaResultResponse();
        response.setUserItemId(null); // No UserItem for GOOD_LUCK
        response.setUserId(userId);
        response.setItemId(null); // No Item for GOOD_LUCK
        response.setItemName("Good luck next time");
        response.setImageUrl(null);
        response.setMultiplier(bannerItem.getMultiplier());
        response.setType("GOOD_LUCK");
        response.setCost(gachaCost);
        response.setPointsRefunded(0);
        response.setObtainedAt(Instant.now());
        return response;
    }

    private GachaResultResponse convertToGachaResponseForDuplicateReward(BannerItem bannerItem, int gachaCost, int pointsRefunded, Long userId) {
        GachaResultResponse response = new GachaResultResponse();
        response.setUserItemId(null); // No new UserItem created
        response.setUserId(userId);
        response.setItemId(bannerItem.getItem().getId());
        response.setItemName(bannerItem.getItem().getItemName() + " (Duplicate)");
        response.setImageUrl(bannerItem.getItem().getImageUrl());
        response.setMultiplier(bannerItem.getMultiplier());
        response.setType("MAIN_REWARD");
        response.setCost(gachaCost);
        response.setPointsRefunded(pointsRefunded);
        response.setObtainedAt(Instant.now());
        return response;
    }
}
