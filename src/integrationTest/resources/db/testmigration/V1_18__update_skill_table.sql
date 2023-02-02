DELETE FROM SKILL WHERE skill_code IN ('SKILL:ABA5:TEST1','SKILL:ABA5:TEST2','SKILL:ABA5:TEST3','SKILL:ABA5:TEST4',
'SKILL:BFA1:TEST1','SKILL:BFA1:TEST2','SKILL:BFA1:TEST3','SKILL:BFA1:TEST4');

INSERT INTO SKILL(skill_id,skill_code,description,service_id,user_type,created_date,last_update) VALUES
	 (26,'SKILL:ABA5:CHECKAPPLICATIONC100','Check application C100','ABA5','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (27,'SKILL:ABA5:GATEKEEPINGC100','Gatekeeping C100','ABA5','Legal Office',timezone('utc', now()),timezone('utc', now())),
	 (28,'SKILL:ABA5:SERVEAPPLICATIONC100','Serve application C100','ABA5','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (29,'SKILL:ABA5:HEARINGMANAGEMENTC100','Hearing management C100','ABA5','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (30,'SKILL:ABA5:ORDERMANAGEMENTC100','Order management C100','ABA5','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (31,'SKILL:ABA5:REVIEWCORRESPONDENCEC100','Review correspondence C100','ABA5','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (32,'SKILL:ABA5:CHECKAPPLICATIONFL401','Check application fl401','ABA5','Future Operations',timezone('utc', now()),timezone('utc', now())),
	 (33,'SKILL:ABA5:GATEKEEPINGFL401','Gate keeping fl401','ABA5','Legal Office',timezone('utc', now()),timezone('utc', now())),
	 (34,'SKILL:ABA5:SERVEAPPLICATIONFL401','Serve application fl401','ABA5','Future Operations',timezone('utc', now()),timezone('utc', now())),
	 (35,'SKILL:ABA5:HEARINGMANAGEMENTFL401','Hearing management fl401','ABA5','Future Operations',timezone('utc', now()),timezone('utc', now())),
	 (36,'SKILL:ABA5:ORDERMANAGEMENTFL401','Order management fl401','ABA5','Future Operations',timezone('utc', now()),timezone('utc', now())),
	 (37,'SKILL:ABA5:REVIEWCORRESPONDENCEFL401','Review correspondence fl401','ABA5','Future Operations',timezone('utc', now()),timezone('utc', now()));