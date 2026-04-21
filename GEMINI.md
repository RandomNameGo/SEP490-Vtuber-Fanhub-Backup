- When generating Mermaid sequence diagrams, follow these rules:
1. Use `actor Client` (not participant) - actors MUST have activation bars (activate/deactivate)
2. Do NOT use `autonumber` directive
3. Use a SINGLE shared `participant DB as Database` lifeline - all repositories interact with it
4. Return messages MUST come BEFORE `deactivate` statements so activation bars cover return arrows
5. Place `deactivate` inside `alt/else` branches before return messages to avoid draw.io "inactive participant" errors
6. Keep diagrams linear where possible; avoid complex nested alt blocks that cause rendering issues
7. Format: actor → Controller → Service → Repository → Database (with activation bars on each)


- When implement code following this rule
1. Read my code base first
2. Following my code style and code structure