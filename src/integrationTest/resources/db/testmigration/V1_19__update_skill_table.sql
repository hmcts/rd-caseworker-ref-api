DELETE FROM SKILL WHERE skill_code IN ('SKILL:ABA5:GATEKEEPINGC100','SKILL:ABA5:GATEKEEPINGFL401');

INSERT INTO SKILL(skill_id,skill_code,description,service_id,user_type,created_date,last_update) VALUES
	 (38,'SKILL:ABA5:GATEKEEPING','Gatekeeping','ABA5','Legal Office',timezone('utc', now()),timezone('utc', now()));