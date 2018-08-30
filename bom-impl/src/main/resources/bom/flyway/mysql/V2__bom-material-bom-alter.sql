drop table bom_material_entity;

create table bom_bom_material (
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

alter table bom_bom_material
	add constraint FKmaxj146b3p1b37os4ml3c301a foreign key (material_id)
	references bom_bom (id);

alter table bom_bom_material
	add constraint FKbhd5lwhweewasacodn5wcw2ya foreign key (bom_id)
	references bom_bom (id);
