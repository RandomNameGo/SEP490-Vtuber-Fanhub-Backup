package com.sep490.vtuber_fanhub.repositories;

import com.sep490.vtuber_fanhub.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p where p.hub.id = :fanHubId and p.status = :status and p.hub.isActive = true")
    Page<Post> findByHubIdAndStatus(Long fanHubId, String status, Pageable pageable);

    @Query("select distinct p from Post p " +
            "left join PostHashtag ph on p.id = ph.post.id " +
            "where p.hub.id = :fanHubId " +
            "and p.status = :status " +
            "and p.hub.isActive = true " +
            "and (:hashtag is null or ph.hashtag = :hashtag)")
    Page<Post> findByHubIdAndStatusAndHashtag(
            Long fanHubId,
            String status,
            String hashtag,
            Pageable pageable);

    @Query("select distinct p from Post p " +
            "left join PostHashtag ph on p.id = ph.post.id " +
            "where p.hub.id = :fanHubId " +
            "and p.status = :status " +
            "and p.hub.isActive = true " +
            "and (:hashtag is null or ph.hashtag = :hashtag) " +
            "and (:authorUsername is null or p.user.username = :authorUsername)")
    Page<Post> findByHubIdAndStatusAndHashtagAndAuthor(
            Long fanHubId,
            String status,
            String hashtag,
            String authorUsername,
            Pageable pageable);

    //Find posts from specific hub IDs (user's followed hubs)
    @Query("select p from Post p " +
            "where p.hub.id in :hubIds " +
            "and p.status = 'APPROVED' " +
            "and p.hub.isActive = true " +
            "order by p.createdAt desc")
    Page<Post> findByHubIdInAndStatusApproved(List<Long> hubIds, Pageable pageable);

    //Find public posts with similar categories for suggestions
    @Query("select distinct p from Post p " +
            "join FanHubCategory fc on p.hub.id = fc.hub.id " +
            "where p.hub.isPrivate = false " +
            "and p.hub.isActive = true " +
            "and p.hub.id not in :excludedHubIds " +
            "and p.status = 'APPROVED' " +
            "and fc.categoryName in :categories " +
            "order by p.createdAt desc")
    Page<Post> findPublicPostsByCategories(
            List<Long> excludedHubIds,
            List<String> categories,
            Pageable pageable);

    //Find any public posts (fallback for suggestions)
    @Query("select p from Post p " +
            "where p.hub.isPrivate = false " +
            "and p.hub.isActive = true " +
            "and p.hub.id not in :excludedHubIds " +
            "and p.status = 'APPROVED' " +
            "order by p.createdAt desc")
    Page<Post> findPublicPosts(
            List<Long> excludedHubIds,
            Pageable pageable);

    //Find public posts sorted by interaction count
    @Query("select p from Post p " +
            "left join PostLike pl on p.id = pl.post.id " +
            "left join PostComment pc on p.id = pc.post.id " +
            "where p.hub.isPrivate = false " +
            "and p.hub.isActive = true " +
            "and p.status = 'APPROVED' " +
            "group by p.id " +
            "order by count(distinct pl.id) + count(distinct pc.id) desc, p.createdAt desc")
    Page<Post> findPublicPostsOrderByInteractions(Pageable pageable);


     //Get categories from user's followed hubs
    @Query("select distinct fc.categoryName from FanHubCategory fc " +
            "where fc.hub.id in :hubIds and fc.hub.isActive = true")
    List<String> findCategoriesByHubIds(List<Long> hubIds);

    // Find trending posts by FanHub: APPROVED posts ordered by likes + comments count desc
    @Query("select p from Post p " +
            "left join PostLike pl on p.id = pl.post.id " +
            "left join PostComment pc on p.id = pc.post.id " +
            "where p.hub.id = :fanHubId " +
            "and p.status = 'APPROVED' " +
            "and p.hub.isActive = true " +
            "group by p.id " +
            "order by count(distinct pl.id) + count(distinct pc.id) desc, p.createdAt desc")
    Page<Post> findTrendingPostsByFanHub(Long fanHubId, Pageable pageable);

    // Find the most interacted post across all public FanHubs in the last 24 hours
    @Query("select p from Post p " +
            "left join PostLike pl on p.id = pl.post.id and pl.createdAt >= :oneDayAgo " +
            "left join PostComment pc on p.id = pc.post.id and pc.createdAt >= :oneDayAgo " +
            "left join VoteOption vo on p.id = vo.post.id " +
            "left join PostVote pv on vo.id = pv.option.id and pv.votedAt >= :oneDayAgo " +
            "where p.hub.isPrivate = false " +
            "and p.hub.isActive = true " +
            "and p.status = 'APPROVED' " +
            "group by p.id " +
            "order by (count(distinct pl.id) + count(distinct pc.id) + count(distinct pv.id)) desc, p.createdAt desc")
    List<Post> findTrendingPost(@Param("oneDayAgo") Instant oneDayAgo, Pageable pageable);

    // Find posts by username with pagination
    @Query("select p from Post p " +
            "where p.user.username = :username " +
            "and p.hub.isActive = true " +
            "order by p.createdAt desc")
    Page<Post> findByUsername(String username, Pageable pageable);

    // Find posts by hub id excluding DELETED status
    @Query("select p from Post p " +
            "where p.hub.id = :fanHubId " +
            "and p.status != 'DELETED' " +
            "and p.hub.isActive = true " +
            "order by p.createdAt desc")
    Page<Post> findByHubIdAndStatusNotDeleted(Long fanHubId, Pageable pageable);

    // Find posts by AI validation status
    @Query("select p from Post p " +
            "where p.hub.id = :fanHubId " +
            "and p.finalAiValidationStatus = :aiStatus " +
            "and p.status = 'PENDING' " +
            "and p.hub.isActive = true " +
            "order by p.createdAt desc")
    List<Post> findByHubIdAndAiValidationStatusAndPending(Long fanHubId, String aiStatus);

    // Find announcement or schedule posts by hub id
    @Query("select p from Post p " +
            "where p.hub.id = :fanHubId " +
            "and p.status = :status " +
            "and p.hub.isActive = true " +
            "and (p.isAnnouncement = true or p.isSchedule = true)")
    Page<Post> findByHubIdAndStatusAndAnnouncementOrSchedule(
            Long fanHubId,
            String status,
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.hub.id = :fanHubId AND p.status != 'DELETED' AND p.status != 'REJECTED' AND p.hub.isActive = true")
    long countPostsByHubId(@Param("fanHubId") Long fanHubId);

    // Search posts by title or content containing keyword
    // Note: Using CAST on content because it's a @Lob field and cannot use LIKE directly
    @Query("SELECT p FROM Post p " +
            "WHERE p.status = 'APPROVED' " +
            "AND p.hub.isActive = true " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(CAST(p.content AS STRING)) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchPosts(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT ph.hashtag FROM PostHashtag ph GROUP BY ph.hashtag ORDER BY COUNT(ph.post.id) DESC")
    List<String> findTrendingHashtags(Pageable pageable);

    @Query("select p from Post p " +
            "where p.hub.isPrivate = false " +
            "and p.hub.isActive = true " +
            "and p.status = 'APPROVED' " +
            "order by p.createdAt desc")
    List<Post> findLatestPublicApprovedPost(Pageable pageable);
    }