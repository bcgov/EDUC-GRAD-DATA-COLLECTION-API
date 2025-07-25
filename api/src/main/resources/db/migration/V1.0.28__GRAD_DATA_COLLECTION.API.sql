ALTER TABLE DEMOGRAPHIC_STUDENT DROP CONSTRAINT FK_DEMOGRAPHIC_STUDENT_INCOMING_FILESET_ID;
ALTER TABLE DEMOGRAPHIC_STUDENT ADD CONSTRAINT FK_DEMOGRAPHIC_STUDENT_INCOMING_FILESET_ID FOREIGN KEY (INCOMING_FILESET_ID) REFERENCES INCOMING_FILESET(INCOMING_FILESET_ID) ON DELETE CASCADE;

ALTER TABLE COURSE_STUDENT DROP CONSTRAINT FK_COURSE_STUDENT_INCOMING_FILESET_ID;
ALTER TABLE COURSE_STUDENT ADD CONSTRAINT FK_COURSE_STUDENT_INCOMING_FILESET_ID FOREIGN KEY (INCOMING_FILESET_ID) REFERENCES INCOMING_FILESET(INCOMING_FILESET_ID) ON DELETE CASCADE;

ALTER TABLE ASSESSMENT_STUDENT DROP CONSTRAINT FK_ASSESSMENT_STUDENT_INCOMING_FILESET_ID;
ALTER TABLE ASSESSMENT_STUDENT ADD CONSTRAINT FK_ASSESSMENT_STUDENT_INCOMING_FILESET_ID FOREIGN KEY (INCOMING_FILESET_ID) REFERENCES INCOMING_FILESET(INCOMING_FILESET_ID) ON DELETE CASCADE;

ALTER TABLE ERROR_FILESET_STUDENT DROP CONSTRAINT FK_ERROR_FILESET_STUDENT_INCOMING_FILESET_ID;
ALTER TABLE ERROR_FILESET_STUDENT ADD CONSTRAINT FK_ERROR_FILESET_STUDENT_INCOMING_FILESET_ID FOREIGN KEY (INCOMING_FILESET_ID) REFERENCES INCOMING_FILESET(INCOMING_FILESET_ID) ON DELETE CASCADE;

ALTER TABLE DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE ADD CONSTRAINT FK_DEM_STUD_VALID_ISSUE_DEM_STUD_ID FOREIGN KEY (DEMOGRAPHIC_STUDENT_ID) REFERENCES DEMOGRAPHIC_STUDENT(DEMOGRAPHIC_STUDENT_ID) ON DELETE CASCADE;
ALTER TABLE COURSE_STUDENT_VALIDATION_ISSUE ADD CONSTRAINT FK_COURSE_STUD_VALID_ISSUE_COURSE_STUD_ID FOREIGN KEY (COURSE_STUDENT_ID) REFERENCES COURSE_STUDENT(COURSE_STUDENT_ID) ON DELETE CASCADE;
ALTER TABLE ASSESSMENT_STUDENT_VALIDATION_ISSUE ADD CONSTRAINT FK_ASSMT_STUD_VALID_ISSUE_ASSMT_STUD_ID FOREIGN KEY (ASSESSMENT_STUDENT_ID) REFERENCES ASSESSMENT_STUDENT(ASSESSMENT_STUDENT_ID) ON DELETE CASCADE;
