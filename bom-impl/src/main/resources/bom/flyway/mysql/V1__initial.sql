create table bom_bom (
	id varchar(50) not null,
	determined_by_id varchar(50),
	determined_by_name varchar(50),
	determined_date datetime,
	drafted_by_id varchar(50),
	drafted_by_name varchar(50),
	drafted_date datetime,
	estimated_accumulated_unit_cost_direct_labor decimal(19,2),
	estimated_accumulated_unit_cost_direct_material decimal(19,2),
	estimated_accumulated_unit_cost_indirect_expenses decimal(19,2),
	estimated_accumulated_unit_cost_indirect_labor decimal(19,2),
	estimated_accumulated_unit_cost_indirect_material decimal(19,2),
	estimated_accumulated_unit_cost_total decimal(19,2),
	estimated_isolated_unit_cost_direct_labor decimal(19,2),
	estimated_isolated_unit_cost_direct_material decimal(19,2),
	estimated_isolated_unit_cost_indirect_expenses decimal(19,2),
	estimated_isolated_unit_cost_indirect_labor decimal(19,2),
	estimated_isolated_unit_cost_indirect_material decimal(19,2),
	estimated_isolated_unit_cost_total decimal(19,2),
	item_id varchar(50),
	last_modified_by_id varchar(50),
	last_modified_by_name varchar(50),
	last_modified_date datetime,
	process_id varchar(50),
	process_name varchar(50),
	revision integer,
	status varchar(20),
	primary key (id)
) engine=InnoDB;

create table bom_material_entity (
	created_by_id varchar(50),
	created_by_name varchar(50),
	created_date datetime,
	estimated_accumulated_unit_cost_direct_labor decimal(19,2),
	estimated_accumulated_unit_cost_direct_material decimal(19,2),
	estimated_accumulated_unit_cost_indirect_expenses decimal(19,2),
	estimated_accumulated_unit_cost_indirect_labor decimal(19,2),
	estimated_accumulated_unit_cost_indirect_material decimal(19,2),
	estimated_accumulated_unit_cost_total decimal(19,2),
	estimated_isolated_unit_cost_direct_labor decimal(19,2),
	estimated_isolated_unit_cost_direct_material decimal(19,2),
	estimated_isolated_unit_cost_indirect_expenses decimal(19,2),
	estimated_isolated_unit_cost_indirect_labor decimal(19,2),
	estimated_isolated_unit_cost_indirect_material decimal(19,2),
	estimated_isolated_unit_cost_total decimal(19,2),
	item_spec_id varchar(50),
	last_modified_by_id varchar(50),
	last_modified_by_name varchar(50),
	last_modified_date datetime,
	quantity decimal(19,5),
	material_id varchar(50) not null,
	bom_id varchar(50) not null,
	primary key (bom_id,material_id)
) engine=InnoDB;

create index BOM_BOM_ITEM_ID_REVISION_IDX
	on bom_bom (item_id,revision);

alter table bom_material_entity
	add constraint UK_eisrwxl2q0ehaw9dkumbx1pg5 unique (bom_id);

alter table bom_material_entity
	add constraint FKf3q622apwqyn3f44v02cdcoqm foreign key (material_id)
	references bom_bom (id);

alter table bom_material_entity
	add constraint FKrltoa5k0xgvld63qk69lqrvdu foreign key (bom_id)
	references bom_bom (id);
