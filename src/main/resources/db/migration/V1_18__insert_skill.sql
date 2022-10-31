-- Staff: Insert Default Skills in SKILL table
INSERT INTO SKILL(skill_id,skill_code,description,service_id,user_type,created_date,last_update) VALUES
	 (1,'SKILL:ABA5:TEST1','testskill1','ABA5','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (2,'SKILL:ABA5:TEST2','testskill2','ABA5','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (3,'SKILL:ABA5:TEST3','testskill3','ABA5','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (4,'SKILL:ABA5:TEST4','testskill4','ABA5','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (5,'SKILL:BFA1:TEST1','testskill1','BFA1','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (6,'SKILL:BFA1:TEST2','testskill2','BFA1','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (7,'SKILL:BFA1:TEST3','testskill3','BFA1','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (8,'SKILL:BFA1:TEST4','testskill4','BFA1','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (9,'SKILL:AAA7:TEST1','testskill1','AAA7','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (10,'SKILL:AAA7:TEST2','testskill2','AAA7','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (11,'SKILL:AAA7:TEST3','testskill3','AAA7','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (12,'SKILL:AAA7:TEST4','testskill4','AAA7','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (13,'SKILL:ABA2:FRSUBMITTEDCASES','testskill5','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (14,'SKILL:ABA2:FRHWFS','testskill6','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (15,'SKILL:ABA2:PAPERCASESFORMA','testskill7','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (16,'SKILL:ABA2:FREXCEPTIONS','testskill8','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (17,'SKILL:ABA2:SUPPLEMENTARYEVIDENCE','testskill9','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (18,'SKILL:ABA2:FRAPPROVEDORDERS','testskill10','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (19,'SKILL:ABA2:FRREFUSEDORDERS','testskill11','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (20,'SKILL:ABA2:RESPONSERECEIVED','testskill12','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (21,'SKILL:ABA2:INFORECEIVED','testskill13','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (22,'SKILL:ABA2:NEWPAPERCASES','testskill14','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (23,'SKILL:ABA2:AWAITINGINFO','testskill15','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (24,'SKILL:ABA2:AWATINGRESPONSE','testskill16','ABA2','CTSC',timezone('utc', now()),timezone('utc', now())),
	 (25,'SKILL:ABA2:NEWDIVORCELAW','testskill17','ABA2','CTSC',timezone('utc', now()),timezone('utc', now()));

COMMIT;
