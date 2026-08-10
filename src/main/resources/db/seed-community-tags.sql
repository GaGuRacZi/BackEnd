-- Idempotent community tag seed (Figma chips).
-- Prefer CommunitySchemaInitializer (runs after Hibernate DDL). Safe if table missing.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = current_schema()
      AND table_name = 'community_tag'
  ) THEN
    INSERT INTO community_tag (tag_name, tag_code, post_type, sort_order, is_active, created_at, updated_at)
    VALUES
      ('건강상담', 'HEALTH_CONSULT', 'COMMUNICATION', 1, true, NOW(), NOW()),
      ('산책친구', 'WALK_BUDDY', 'COMMUNICATION', 2, true, NOW(), NOW()),
      ('헌혈소식', 'BLOOD_NEWS', 'COMMUNICATION', 3, true, NOW(), NOW()),
      ('동네정보', 'LOCAL_INFO', 'COMMUNICATION', 4, true, NOW(), NOW()),
      ('사료·간식', 'FOOD_SNACK', 'MARKET', 1, true, NOW(), NOW()),
      ('용품', 'SUPPLIES', 'MARKET', 2, true, NOW(), NOW()),
      ('소모품', 'CONSUMABLES', 'MARKET', 3, true, NOW(), NOW()),
      ('영양제', 'SUPPLEMENT', 'MARKET', 4, true, NOW(), NOW()),
      ('기타', 'OTHER', 'MARKET', 5, true, NOW(), NOW()),
      ('산책 장소', 'WALK_PLACE', 'REVIEW', 1, true, NOW(), NOW()),
      ('병원', 'HOSPITAL', 'REVIEW', 2, true, NOW(), NOW()),
      ('용품샵', 'SUPPLY_SHOP', 'REVIEW', 3, true, NOW(), NOW()),
      ('미용실', 'GROOMING', 'REVIEW', 4, true, NOW(), NOW())
    ON CONFLICT (post_type, tag_code) DO NOTHING;
  END IF;
END $$;;
