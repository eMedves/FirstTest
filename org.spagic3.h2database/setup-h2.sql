INSERT INTO type_service (id_ts, value) VALUES(1, 'JBI-BINDING COMPONENT');
INSERT INTO type_service (id_ts, value) VALUES(2, 'JBI-SERVICE ENGINE');
INSERT INTO type_service (id_ts, value) VALUES(3, 'GENERIC-PROCESS');
INSERT INTO type_service (id_ts, value) VALUES(4, 'JBI-PROCESS');
INSERT INTO type_service (id_ts, value) VALUES(5, 'BPEL-PROCESS');
INSERT INTO type_service (id_ts, value) VALUES(6, 'JBPM-PROCESS');
INSERT INTO type_service (id_ts, value) VALUES(7, 'OSGI-CONNECTOR');
INSERT INTO type_service (id_ts, value) VALUES(8, 'OSGI-SERVICE');

INSERT INTO transition_state (id_state,state_value) VALUES(0, 'JBI_INONLY_ACTIVE');
INSERT INTO transition_state (id_state,state_value) VALUES(1, 'JBI_INONLY_DONE');
INSERT INTO transition_state (id_state,state_value) VALUES(2, 'JBI_INONLY_FAULT');
INSERT INTO transition_state (id_state,state_value) VALUES(3, 'JBI_INOUT_ACTIVE');
INSERT INTO transition_state (id_state,state_value) VALUES(4, 'JBI_INOUT_OUT');
INSERT INTO transition_state (id_state,state_value) VALUES(5, 'JBI_INOUT_FAULT');
INSERT INTO transition_state (id_state,state_value) VALUES(6, 'JBI_INOUT_DONE');
INSERT INTO transition_state (id_state,state_value) VALUES(7, 'JBI_ROBUSTINONLY_ACTIVE');
INSERT INTO transition_state (id_state,state_value) VALUES(8, 'JBI_ROBUSTINONLY_STEP1_DONE');
INSERT INTO transition_state (id_state,state_value) VALUES(9, 'JBI_ROBUSTINONLY_STEP1_FAULT');
INSERT INTO transition_state (id_state,state_value) VALUES(10, 'JBI_ROBUSTINONLY_DONE');
INSERT INTO transition_state (id_state,state_value) VALUES(11, 'JBI_INOPTIONALOUT_ACTIVE');
INSERT INTO transition_state (id_state,state_value) VALUES(12, 'JBI_INOPTIONALOUT_STEP1_OUT');
INSERT INTO transition_state (id_state,state_value) VALUES(13, 'JBI_INOPTIONALOUT_STEP1_DONE');
INSERT INTO transition_state (id_state,state_value) VALUES(14, 'JBI_INOPTIONALOUT_STEP1_FAULT');
INSERT INTO transition_state (id_state,state_value) VALUES(15, 'JBI_INOPTIONALOUT_STEP2_DONE');
INSERT INTO transition_state (id_state,state_value) VALUES(16, 'JBI_INOPTIONALOUT_STEP2_FAULT');
INSERT INTO transition_state (id_state,state_value) VALUES(17, 'JBI_INOPTIONALOUT_DONE');

INSERT INTO transition_state (id_state,state_value) VALUES(18, 'BPEL_START_ACTIVITY');
INSERT INTO transition_state (id_state,state_value) VALUES(19, 'BPEL_END_ACTIVITY');
INSERT INTO transition_state (id_state,state_value) VALUES(20, 'BPEL_FAULTED_ACTIVITY');

INSERT INTO transition_state (id_state,state_value) VALUES(21, 'JBPM_START_ACTIVITY');
INSERT INTO transition_state (id_state,state_value) VALUES(22, 'JBPM_END_ACTIVITY');
INSERT INTO transition_state (id_state,state_value) VALUES(23, 'JBPM_FAULTED_ACTIVITY');

INSERT INTO xml_namespace (id_namespace,prefix,value) VALUES(1, 'restart', 'urn:it:eng:spagic:restart');
INSERT INTO xml_namespace (id_namespace,prefix,value) VALUES(2, 'spagic', 'urn:it:eng:spagic');
INSERT INTO xml_namespace (id_namespace,prefix,value) VALUES(3, 'spagicevent', 'urn:it:eng:spagic:event');

INSERT INTO pool_type (id_pool_type,id_rule_det_pool_type,name,description,pool_type_version) VALUES(NULL,NULL,'ANONYMOUS','POOL TYPE FOR DYNAMIC LINKS','0');

INSERT INTO process_execution_state (id_process_execution_state,state_value) VALUES(0, 'NORMAL_EXECUTION');
INSERT INTO process_execution_state (id_process_execution_state,state_value) VALUES(1, 'PAUSE_EXECUTION');
INSERT INTO process_execution_state (id_process_execution_state,state_value) VALUES(2, 'STOP_EXECUTION');

INSERT INTO monitoring_type (id_monitoring_type,type_value) VALUES(0, 'ASYNCHRONOUS');
INSERT INTO monitoring_type (id_monitoring_type,type_value) VALUES(1, 'SYNCHRONOUS');

INSERT INTO log_journal_type (id_log_journal_type, type_journal) VALUES(0, 'MESSAGE');
INSERT INTO log_journal_type (id_log_journal_type, type_journal) VALUES(1, 'VARIABLE');
	 