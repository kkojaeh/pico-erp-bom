ALTER TABLE bom_bom DROP process_id;
ALTER TABLE bom_bom DROP process_name;

create table bom_bom_process (
	id binary(16) not null,
	bom_id binary(16),
	conversion_rate decimal(19,5),
	created_by_id varchar(50),
	created_by_name varchar(50),
	created_date datetime,
	last_modified_by_id varchar(50),
	last_modified_by_name varchar(50),
	last_modified_date datetime,
	process_order integer,
	process_id binary(16),
	primary key (id)
) engine=InnoDB;
