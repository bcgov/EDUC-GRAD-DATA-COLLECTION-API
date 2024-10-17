--FILESET STATUS CODES

INSERT INTO FILE_STATUS_CODE (FILE_STATUS_CODE, LABEL, DESCRIPTION,
                              DISPLAY_ORDER, CREATE_USER, CREATE_DATE,
                              UPDATE_USER, UPDATE_DATE)
VALUES ('NOTLOADED', 'Not Loaded', 'File has not been uploaded yet', '10', 'API_GRAD_DATA_COLLECTION',
        TO_DATE('20241017', 'YYYYMMDD'), 'API_GRAD_DATA_COLLECTION', TO_DATE('20241017', 'YYYYMMDD'));

INSERT INTO FILE_STATUS_CODE (FILE_STATUS_CODE, LABEL, DESCRIPTION,
                              DISPLAY_ORDER, CREATE_USER, CREATE_DATE,
                              UPDATE_USER, UPDATE_DATE)
VALUES ('LOADED', 'Loaded', 'One or more files have been uploaded', '10', 'API_GRAD_DATA_COLLECTION',
        TO_DATE('20241017', 'YYYYMMDD'), 'API_GRAD_DATA_COLLECTION', TO_DATE('20241017', 'YYYYMMDD'));

INSERT INTO FILE_STATUS_CODE (FILE_STATUS_CODE, LABEL, DESCRIPTION,
                              DISPLAY_ORDER, CREATE_USER, CREATE_DATE,
                              UPDATE_USER, UPDATE_DATE)

INSERT INTO FILE_STATUS_CODE (FILE_STATUS_CODE, LABEL, DESCRIPTION,
                              DISPLAY_ORDER, CREATE_USER, CREATE_DATE,
                              UPDATE_USER, UPDATE_DATE)
VALUES ('COMPLETED', 'Complete', 'Files have been processed and updated downstream', '10', 'API_GRAD_DATA_COLLECTION',
        TO_DATE('20241017', 'YYYYMMDD'), 'API_GRAD_DATA_COLLECTION', TO_DATE('20241017', 'YYYYMMDD'));


--STUDENT STATUS CODES

INSERT INTO STUDENT_STATUS_CODE (STUDENT_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                 CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('LOADED', 'Loaded', 'Student record loaded into database', '10',
        'API_GRAD_DATA_COLLECTION',
        TO_DATE('20241017', 'YYYYMMDD'),
        'API_GRAD_DATA_COLLECTION', TO_DATE('20241017', 'YYYYMMDD'));

INSERT INTO STUDENT_STATUS_CODE (STUDENT_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                 CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('ERROR', 'Error', 'Student record has an error', '10',
        'API_GRAD_DATA_COLLECTION',
        TO_DATE('20241017', 'YYYYMMDD'),
        'API_GRAD_DATA_COLLECTION', TO_DATE('20241017', 'YYYYMMDD'));

INSERT INTO STUDENT_STATUS_CODE (STUDENT_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                 CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('WARNING', 'Warning', 'Student record has a warning', '10',
        'API_GRAD_DATA_COLLECTION',
        TO_DATE('20241017', 'YYYYMMDD'),
        'API_GRAD_DATA_COLLECTION', TO_DATE('20241017', 'YYYYMMDD'));