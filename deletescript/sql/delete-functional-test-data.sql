BEGIN;

CREATE TEMP TABLE tmp_cw_ids ON COMMIT DROP AS
SELECT case_worker_id
FROM   dbrdcaseworker.case_worker_profile
WHERE  email_id ILIKE ANY (ARRAY[
         'cwr-rd-func-test-user-only-%',
         'cwr-func-test-user-%',
         'staff-rd-profile-func-test-user-only-%'
       ])
       and created_date <= CURRENT_TIMESTAMP - INTERVAL '1 hour';

-- BEFORE deleting counts
SELECT 'case_worker_location' AS table_name, count(*) AS before_delete_count FROM dbrdcaseworker.case_worker_location WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids)
UNION ALL
SELECT 'case_worker_work_area', count(*) FROM dbrdcaseworker.case_worker_work_area WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids)
UNION ALL
SELECT 'case_worker_role', count(*) FROM dbrdcaseworker.case_worker_role WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids)
UNION ALL
SELECT 'case_worker_skill', count(*) FROM dbrdcaseworker.case_worker_skill WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids)
UNION ALL
SELECT 'case_worker_profile', count(*) FROM dbrdcaseworker.case_worker_profile WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids);

-- Deletes and prints the actual rows removed per table
WITH del_loc  AS (DELETE FROM dbrdcaseworker.case_worker_location  WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids) RETURNING 1),
    del_area  AS (DELETE FROM dbrdcaseworker.case_worker_work_area WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids) RETURNING 1),
    del_role  AS (DELETE FROM dbrdcaseworker.case_worker_role      WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids) RETURNING 1),
    del_skill AS (DELETE FROM dbrdcaseworker.case_worker_skill     WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids) RETURNING 1),
    del_profile AS (DELETE FROM dbrdcaseworker.case_worker_profile WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids) RETURNING 1)
SELECT (SELECT count(*) FROM del_loc)   AS deleted_location,
       (SELECT count(*) FROM del_area)  AS deleted_work_area,
       (SELECT count(*) FROM del_role)  AS deleted_role,
       (SELECT count(*) FROM del_skill) AS deleted_skill,
       (SELECT count(*) FROM del_profile) AS deleted_profile;

-- AFTER deleting counts
SELECT 'case_worker_location' AS table_name, count(*) AS after_delete_count FROM dbrdcaseworker.case_worker_location WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids)
UNION ALL
SELECT 'case_worker_work_area', count(*) FROM dbrdcaseworker.case_worker_work_area WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids)
UNION ALL
SELECT 'case_worker_role', count(*) FROM dbrdcaseworker.case_worker_role WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids)
UNION ALL
SELECT 'case_worker_skill', count(*) FROM dbrdcaseworker.case_worker_skill WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids)
UNION ALL
SELECT 'case_worker_profile', count(*) FROM dbrdcaseworker.case_worker_profile WHERE case_worker_id IN (SELECT case_worker_id FROM tmp_cw_ids);

COMMIT;

