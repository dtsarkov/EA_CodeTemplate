SELECT o.ea_guid AS CLASSGUID, o.Object_Type AS CLASSTYPE ,o.Name ,o.Object_ID
FROM t_object	o
	,t_package	p
WHERE	o.Package_ID = p.Package_ID
AND		p.Name+':'+o.Name = '<Search Term>'