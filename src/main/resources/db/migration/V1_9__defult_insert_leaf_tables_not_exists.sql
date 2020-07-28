insert into judicial_role_type (role_id, role_desc_en,role_desc_cy)
values ('0', 'default', 'default')  on conflict (role_id) do nothing;

insert into contract_type(contract_type_id,contract_type_desc_en,contract_type_desc_cy)
values ('0', 'default', 'default')  on conflict (contract_type_id) do nothing;

insert into base_location_type(base_location_id,court_name,bench,court_type,circuit,area_of_expertise,
national_court_code) values ('0', 'default', 'default','default', 'default','default', 'default')
on conflict (base_location_id) do nothing;

insert into region_type(region_id,region_desc_en,region_desc_cy)
values ('0', 'default', 'default') on conflict (region_id) do nothing;