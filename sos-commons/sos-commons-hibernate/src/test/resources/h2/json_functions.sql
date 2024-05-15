
set MODE MYSQL;

/* necessary for embedded H2 */
DROP ALIAS IF EXISTS SOS_JSON_VALUE;
CREATE ALIAS SOS_JSON_VALUE FOR "com.sos.commons.hibernate.function.json.h2.Functions.jsonValue";

DROP ALIAS IF EXISTS SOS_JSON_ARRAY_LENGTH;
CREATE ALIAS SOS_JSON_ARRAY_LENGTH FOR "com.sos.commons.hibernate.function.json.h2.Functions.jsonArrayLength";
