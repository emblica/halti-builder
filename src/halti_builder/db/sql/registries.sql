-- :name create-registries-table
-- :command :execute
-- :result :raw
-- :doc Create registries table
CREATE TABLE registries
(
    id serial PRIMARY KEY NOT NULL,
    name VARCHAR(120),
    url VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);


-- :name registry-by-id :? :1
-- :doc Get registry by id
SELECT * FROM registries
WHERE id = :id

-- :name all-registries :? :*
-- :doc Get registries
SELECT * FROM registries;
