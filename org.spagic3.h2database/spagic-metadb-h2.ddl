Create table process_instance (
	id_process_instance Bigint(20) NOT NULL AUTO_INCREMENT,
	id_parent_process_instance Bigint(20),
	id_process Bigint NOT NULL,
	startdate Datetime NOT NULL,
	enddate Datetime NOT NULL,
	executiontime Bigint,
	state Int(11) NOT NULL,
	id_correlation Varchar(100) NOT NULL,
	id_pool Bigint,
	id_process_execution_state Bigint(20) NOT NULL DEFAULT 0,
 Primary Key (id_process_instance));

Create table transition_instance (
	id_journal Bigint(20) NOT NULL AUTO_INCREMENT,
	id_step_source Bigint,
	id_step_target Bigint NOT NULL,
	id_process_instance Bigint(20) NOT NULL,
	id_message Varchar(100) NOT NULL,
	transition_start Timestamp NOT NULL,
	transition_end Timestamp,
	transition_execution_time Bigint,
	id_ext_process_instance Bigint(20),
	id_state Int NOT NULL,
	transition_data Text,
	transition_data_out Text,
	attachment_in Int DEFAULT 0,
	attachment_out Int DEFAULT 0,
	runtime_warning Bool NOT NULL DEFAULT false,
 Primary Key (id_journal));

Create table log_journal (
	id_log Bigint(20) NOT NULL AUTO_INCREMENT,
	id_state Int NOT NULL,
	id_journal Bigint NOT NULL,
	name Varchar(200),
	log Text NOT NULL,
	datelog Datetime NOT NULL,
	id_log_journal_type Bigint NOT NULL DEFAULT 0,
 Primary Key (id_log));

Create table property (
	id_property Bigint(20) NOT NULL AUTO_INCREMENT,
	code Varchar(200) NOT NULL,
	value Varchar(10000) NOT NULL,
 Primary Key (id_property));

Create table property_process (
	id_property Bigint(20) NOT NULL,
	id_process Bigint NOT NULL,
 Primary Key (id_property,id_process));

Create table process (
	id_process Bigint(20) NOT NULL AUTO_INCREMENT,
	process_version Varchar(20) NOT NULL,
	namespace Varchar(200) NOT NULL,
	name Varchar(200) NOT NULL,
	description Varchar(200),
	id_ts Bigint(20) NOT NULL,
	uddi_org Varchar(200),
	uddi_name Varchar(200),
	startdate Datetime NOT NULL,
	enddate Datetime,
	state Int(10) UNSIGNED NOT NULL,
	monitor_enabled Bool DEFAULT true,
	id_monitoring_type Bigint(20) NOT NULL DEFAULT 0,
 Primary Key (id_process));

Create table step (
	id_step Bigint NOT NULL AUTO_INCREMENT,
	namespace Varchar(200) NOT NULL,
	name Varchar(200) NOT NULL,
	description Varchar(200),
	is_start Bool NOT NULL,
	is_end Bool NOT NULL,
	id_process Bigint NOT NULL,
	process_version Char(20) NOT NULL,
	has_conditional_exit Bool,
	documentation MEDIUMBLOB  DEFAULT NULL,
	documentationMimeType VARCHAR(150)  DEFAULT NULL,
	documentationFileName VARCHAR(150)  DEFAULT NULL,
	state Int(10) UNSIGNED NOT NULL DEFAULT 0,
 Primary Key (id_step));

Create table type_service (
	id_ts Bigint(20) NOT NULL AUTO_INCREMENT,
	value Varchar(45) NOT NULL,
 Primary Key (id_ts));

Create table property_step (
	id_step Bigint NOT NULL,
	id_property Bigint NOT NULL,
 Primary Key (id_step,id_property));

Create table transition_state (
	id_state Int NOT NULL,
	state_value Varchar(100),
 Primary Key (id_state));

Create table process_diagram (
	svg_diag Mediumtext,
	id_process Bigint NOT NULL,
 Primary Key (id_process));

Create table rule (
	id_rule Bigint NOT NULL AUTO_INCREMENT,
	id_attribute Bigint,
	transition_state Int NOT NULL,
	condition_expr Varchar(300),
	expr Varchar(300) NOT NULL,
	name Varchar(200) NOT NULL,
	start_date Date,
	end_date Date,
	rule_type Bigint,
	rule_usage Int NOT NULL,
 Primary Key (id_rule));

Create table relevant_data (
	id_relevant_data Bigint NOT NULL AUTO_INCREMENT,
	id_attribute Bigint NOT NULL,
	id_log Bigint NOT NULL,
	value Varchar(500) NOT NULL,
 Primary Key (id_relevant_data));

Create table CSCHEME (
	CSCHEME_ID Varchar(50) NOT NULL,
	CSCHEME_NAME Varchar(100) NOT NULL,
	CSCHEME_DESCRIPTION Varchar(200),
 Primary Key (CSCHEME_ID));

Create table CSCHEME_VALUES (
	CSCHEME_ID Varchar(50) NOT NULL,
	CSCHEME_VALUE Varchar(200) NOT NULL,
 Primary Key (CSCHEME_ID,CSCHEME_VALUE));

Create table SERVICE_CLASSIFICATION (
	CSCHEME_ID Varchar(50) NOT NULL,
	CSCHEME_VALUE Varchar(200) NOT NULL,
	service_uddi_org Varchar(200) NOT NULL,
	service_uddi_name Varchar(200) NOT NULL,
 Primary Key (CSCHEME_ID,CSCHEME_VALUE,service_uddi_org,service_uddi_name));

Create table step_rule (
	id_step Bigint NOT NULL,
	id_rule Bigint NOT NULL,
	order_id Int NOT NULL,
 Primary Key (id_step,id_rule));

Create table catalog (
	id_catalog Bigint NOT NULL AUTO_INCREMENT,
	name Varchar(200) NOT NULL,
	visible Bool,
	mandatory Bool,
 Primary Key (id_catalog));

Create table attribute (
	id_attribute Bigint NOT NULL AUTO_INCREMENT,
	name Varchar(50) NOT NULL,
	type Int NOT NULL,
	UNIQUE (name),
 Primary Key (id_attribute));

Create table transition (
	id_step_source Bigint NOT NULL,
	id_step_target Bigint NOT NULL,
	id_process Bigint NOT NULL,
 Primary Key (id_step_source,id_step_target));

Create table pool_type (
	id_pool_type Bigint NOT NULL AUTO_INCREMENT,
	id_rule_det_pool_type Bigint,
	name Varchar(200) NOT NULL,
	description Varchar(200) NOT NULL,
	pool_type_version Char(20) NOT NULL,
 Primary Key (id_pool_type));

Create table pool_instance (
	id_pool Bigint NOT NULL AUTO_INCREMENT,
	id_pool_type Bigint NOT NULL,
	unique_pool_instance_key Varchar(1000) NOT NULL,
 Primary Key (id_pool));

Create table pool_type_process (
	id_process Bigint NOT NULL,
	order_criteria Int NOT NULL,
	id_pool_type Bigint NOT NULL,
 Primary Key (id_process,id_pool_type));

Create table relevant_data_process_instance (
	id_process_instance Bigint NOT NULL,
	id_relevant_data Bigint NOT NULL,
 Primary Key (id_process_instance,id_relevant_data));

Create table xml_namespace (
	id_namespace Bigint NOT NULL AUTO_INCREMENT,
	prefix Varchar(50) NOT NULL,
	value Varchar(200) NOT NULL,
 Primary Key (id_namespace));

Create table attribute_pool (
	id_attribute Bigint NOT NULL,
	id_pool_type Bigint NOT NULL,
 Primary Key (id_attribute,id_pool_type));

Create table attribute_catalog (
	id_attribute Bigint NOT NULL,
	id_catalog Bigint NOT NULL,
	visible Bool,
	order_criteria Int,
 Primary Key (id_attribute,id_catalog));

Create table wait_data (
	id_wait Bigint(20) NOT NULL AUTO_INCREMENT,
	id_correlation Varchar(200) NOT NULL,
	wait_timestamp Timestamp NOT NULL,
	expiry_timestamp Timestamp NULL DEFAULT NULL,
	expiration_action Varchar(500),
	eventType Varchar(200) DEFAULT 'GenericEvent' NOT NULL,
	exchange Blob,
	signalUri Varchar(500),
	destinationUri Varchar(500),
 Primary Key (id_wait));

Create table wait_data_attribute (
	id_wait_data_attribute Bigint NOT NULL AUTO_INCREMENT,
	id_wait Bigint NOT NULL,
	name Varchar(200) NOT NULL,
	att_value Varchar(500),
 Primary Key (id_wait_data_attribute));

Create table search_query (
   id_query Bigint(20) NOT NULL AUTO_INCREMENT,
   label Varchar(100) NOT NULL UNIQUE,
   query Text NOT NULL,
Primary Key (id_query));

Create table sub_process (
	id_process Bigint NOT NULL,
	id_step Bigint NOT NULL,
	id_subprocess Bigint NOT NULL,
	descr Varchar(50),
 Primary Key (id_process,id_step,id_subprocess));

Create table process_execution_state (
	id_process_execution_state Bigint(20) NOT NULL,
	state_value Varchar(100),
 Primary Key (id_process_execution_state));

Create table monitoring_type (
	id_monitoring_type Bigint(20) NOT NULL,
	type_value Varchar(100),
 Primary Key (id_monitoring_type));

Create table temporary_process_instance (
	id_process_instance Bigint(20) NOT NULL,
 Primary Key (id_process_instance));

Create table variable_instance (
	id_variable_instance Bigint NOT NULL AUTO_INCREMENT,
	id_variable_definition Bigint,
	name Varchar(100) NOT NULL,
	value Longtext,
	id_process_instance Bigint NOT NULL,
 Primary Key (id_variable_instance));

Create table variable_definition (
	id_variable_definition Bigint NOT NULL AUTO_INCREMENT,
	name Varchar(100) NOT NULL,
	varSchema Longtext,
	id_process Bigint NOT NULL,
 Primary Key (id_variable_definition));

Create table log_journal_type (
	id_log_journal_type Bigint NOT NULL,
	type_journal Varchar(50) NOT NULL,
 Primary Key (id_log_journal_type));

CREATE VIEW bc_input_messages AS
  SELECT s.id_step AS id_binding_component, COUNT(logJournal.id_log) AS input_messages 
	FROM step s, process p, transition_instance t, log_journal logJournal,property_step propertyStep, property pty  	
	WHERE s.id_process = p.id_process AND s.id_process = (SELECT max(pSub.id_process) AS idProcess FROM process pSub WHERE pSub.name=p.name)				
				AND s.id_step = t.id_step_source
				AND t.id_journal = logJournal.id_journal 				
				AND s.id_step=propertyStep.id_step 				
				AND propertyStep.id_property=pty.id_property
				AND pty.code='im.servicebindingname'
				AND ((s.is_start = 1 AND logJournal.id_state IN (0, 3)) 
						OR (pty.value IN ('JBI-WebServicePipeline','JBI-TCPIP-Pipeline','JBI-CXF-Pipeline') AND logJournal.id_state=3 )
						) 				
	group by s.id_step;
	
CREATE VIEW bc_output_messages AS
  SELECT s.id_step AS id_binding_component, COUNT(logJournal.id_log) AS output_messages 
	FROM step s, process p, transition_instance t, log_journal logJournal,property_step propertyStep, property pty  	
	WHERE s.id_process = p.id_process AND s.id_process = (SELECT max(pSub.id_process) AS idProcess FROM process pSub WHERE pSub.name=p.name)								
				AND t.id_journal = logJournal.id_journal 				
				AND s.id_step=propertyStep.id_step 				
				AND propertyStep.id_property=pty.id_property
				AND pty.code='im.servicebindingname'
				AND ((s.is_end = 1 AND logJournal.id_state=1 AND s.id_step = t.id_step_target)
						OR (pty.value IN ('JBI-WebServicePipeline','JBI-TCPIP-Pipeline','JBI-CXF-Pipeline') AND s.id_step = t.id_step_source AND logJournal.id_state=4 )
            OR (s.is_start = 1 AND logJournal.id_state = 4 AND s.id_step = t.id_step_source)						
						) 				
	group by s.id_step;
	
CREATE VIEW bc_error_messages AS
  SELECT s.id_step AS id_binding_component, COUNT(logJournal.id_log) AS error_messages 
	FROM step s, process p, transition_instance t, log_journal logJournal,property_step propertyStep, property pty  	
	WHERE s.id_process = p.id_process AND s.id_process = (SELECT max(pSub.id_process) AS idProcess FROM process pSub WHERE pSub.name=p.name)								
				AND t.id_journal = logJournal.id_journal 				
				AND s.id_step=propertyStep.id_step 				
				AND propertyStep.id_property=pty.id_property
				AND pty.code='im.servicebindingname'
				AND ((s.is_start = 1 AND s.id_step = t.id_step_source AND logJournal.id_state IN (2, 5))
						OR(s.is_end = 1 AND s.id_step = t.id_step_target AND logJournal.id_state IN (2, 5))
						OR (pty.value IN ('JBI-WebServicePipeline','JBI-TCPIP-Pipeline','JBI-CXF-Pipeline') AND logJournal.id_state=5
								AND (s.id_step = t.id_step_target OR s.id_step = t.id_step_source))
						) 				
	group by s.id_step;
	

Create Index fk_p_sa  ON process_instance (id_process);
Create Index fk_jip_p  ON transition_instance (id_process_instance);
Create Index fk_sa_ts  ON process (id_ts);

Create Index idx_process_instance_01 ON process_instance (id_correlation);
Create Index idx_transition_instance_01 ON transition_instance (id_message);
Create Index idx_wait_data_01 ON wait_data (id_correlation);
Create Index idx_log_journal_01 ON log_journal (id_journal);
Create Index idx_log_journal_02 ON log_journal (id_state);


Alter table transition_instance add Foreign Key (id_process_instance) references process_instance (id_process_instance) on delete  restrict on update  restrict;
Alter table relevant_data_process_instance add Foreign Key (id_process_instance) references process_instance (id_process_instance) on delete  restrict on update  restrict;
Alter table process_instance add Foreign Key (id_parent_process_instance) references process_instance (id_process_instance) on delete  restrict on update  restrict;
Alter table log_journal add Foreign Key (id_journal) references transition_instance (id_journal) on delete  restrict on update  restrict;
Alter table relevant_data add Foreign Key (id_log) references log_journal (id_log) on delete  restrict on update  restrict;
Alter table property_process add Foreign Key (id_property) references property (id_property) on delete  restrict on update  restrict;
Alter table property_step add Foreign Key (id_property) references property (id_property) on delete  restrict on update  restrict;
Alter table process_instance add Foreign Key (id_process) references process (id_process) on delete  restrict on update  restrict;
Alter table property_process add Foreign Key (id_process) references process (id_process) on delete  restrict on update  restrict;
Alter table step add Foreign Key (id_process) references process (id_process) on delete  restrict on update  restrict;
Alter table process_diagram add Foreign Key (id_process) references process (id_process) on delete  restrict on update  restrict;
Alter table transition add Foreign Key (id_process) references process (id_process) on delete  restrict on update  restrict;
Alter table pool_type_process add Foreign Key (id_process) references process (id_process) on delete  restrict on update  restrict;
Alter table transition_instance add Foreign Key (id_step_source) references step (id_step) on delete  restrict on update  restrict;
Alter table property_step add Foreign Key (id_step) references step (id_step) on delete  restrict on update  restrict;
Alter table transition_instance add Foreign Key (id_step_target) references step (id_step) on delete  restrict on update  restrict;
Alter table step_rule add Foreign Key (id_step) references step (id_step) on delete  restrict on update  restrict;
Alter table transition add Foreign Key (id_step_source) references step (id_step) on delete  restrict on update  restrict;
Alter table transition add Foreign Key (id_step_target) references step (id_step) on delete  restrict on update  restrict;
Alter table process add Foreign Key (id_ts) references type_service (id_ts) on delete  restrict on update  restrict;
Alter table transition_instance add Foreign Key (id_state) references transition_state (id_state) on delete  restrict on update  restrict;
Alter table log_journal add Foreign Key (id_state) references transition_state (id_state) on delete  restrict on update  restrict;
Alter table step_rule add Foreign Key (id_rule) references rule (id_rule) on delete  restrict on update  restrict;
Alter table pool_type add Foreign Key (id_rule_det_pool_type) references rule (id_rule) on delete  restrict on update  restrict;
Alter table relevant_data_process_instance add Foreign Key (id_relevant_data) references relevant_data (id_relevant_data) on delete  restrict on update  restrict;
Alter table CSCHEME_VALUES add Foreign Key (CSCHEME_ID) references CSCHEME (CSCHEME_ID) on delete  restrict on update  restrict;
Alter table SERVICE_CLASSIFICATION add Foreign Key (CSCHEME_ID,CSCHEME_VALUE) references CSCHEME_VALUES (CSCHEME_ID,CSCHEME_VALUE) on delete  restrict on update  restrict;
Alter table attribute_catalog add Foreign Key (id_catalog) references catalog (id_catalog) on delete  restrict on update  restrict;
Alter table relevant_data add Foreign Key (id_attribute) references attribute (id_attribute) on delete  restrict on update  restrict;
Alter table rule add Foreign Key (id_attribute) references attribute (id_attribute) on delete  restrict on update  restrict;
Alter table attribute_pool add Foreign Key (id_attribute) references attribute (id_attribute) on delete  restrict on update  restrict;
Alter table attribute_catalog add Foreign Key (id_attribute) references attribute (id_attribute) on delete  restrict on update  restrict;
Alter table pool_instance add Foreign Key (id_pool_type) references pool_type (id_pool_type) on delete  restrict on update  restrict;
Alter table pool_type_process add Foreign Key (id_pool_type) references pool_type (id_pool_type) on delete  restrict on update  restrict;
Alter table attribute_pool add Foreign Key (id_pool_type) references pool_type (id_pool_type) on delete  restrict on update  restrict;
Alter table process_instance add Foreign Key (id_pool) references pool_instance (id_pool) on delete  restrict on update  restrict;
Alter table wait_data_attribute add Foreign Key (id_wait) references wait_data (id_wait) on delete  restrict on update  restrict;
Alter table sub_process add Foreign Key (id_process) references process (id_process) on delete  restrict on update  restrict;
Alter table sub_process add Foreign Key (id_subprocess) references process (id_process) on delete  restrict on update  restrict;
Alter table sub_process add Foreign Key (id_step) references step (id_step) on delete  restrict on update  restrict;

Alter table process_instance add Foreign Key (id_process_execution_state) references process_execution_state (id_process_execution_state) on delete  restrict on update  restrict;
Alter table process add Foreign Key (id_monitoring_type) references monitoring_type (id_monitoring_type) on delete  restrict on update  restrict;

Alter table variable_instance add Foreign Key (id_process_instance) references process_instance (id_process_instance) on delete  restrict on update  restrict;
Alter table variable_definition add Foreign Key (id_process) references process (id_process) on delete  restrict on update  restrict;
Alter table variable_instance add Foreign Key (id_variable_definition) references variable_definition (id_variable_definition) on delete  restrict on update  restrict;
