-- :name create-source-table
-- :command :execute
-- :result :raw
-- :doc Create source table
CREATE TABLE sources
(
    id serial PRIMARY KEY NOT NULL,
    url VARCHAR(1024),
    path VARCHAR(1024),
    branch VARCHAR(512) DEFAULT 'master',
    private_key TEXT,
    public_key TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);


CREATE TABLE images
(
  id serial PRIMARY KEY NOT NULL,
  name VARCHAR(200),
  source INTEGER,
  registry INTEGER,
  description TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
)


-- :name image-by-id :? :1
-- :doc Get image by id
SELECT
    images.id as id,
    images.name,
    images.description,
    registries.id as registry_id,
    sources.id as source_id,
    registries.created_at as registry_created_at,
    sources.created_at as source_created_at,
    registries.url as registry_url,
    sources.url as source_url,
    sources.public_key,
    sources.private_key,
    sources.path,
    sources.branch
FROM images
  LEFT JOIN registries ON images.registry = registries.id
  LEFT JOIN sources ON images.source = sources.id
WHERE images.id = :id

-- :name all-images :? :*
-- :doc Get images
SELECT
    images.id as id,
    images.name,
    images.description,
    registries.id as registry_id,
    sources.id as source_id,
    registries.created_at as registry_created_at,
    sources.created_at as source_created_at,
    registries.url as registry_url,
    sources.url as source_url,
    sources.public_key,
    sources.private_key,
    sources.path,
    sources.branch,
    json_build_object(
        'id', builds.id,
        'commit', builds.commit_message,
        'author', builds.author,
        'status', builds.status,
        'started', builds.started
    ) as latest_build
FROM images
  LEFT JOIN registries ON images.registry = registries.id
  LEFT JOIN sources ON images.source = sources.id
  LEFT JOIN builds ON builds.id = (
    SELECT
      MAX(builds.id) as id
    FROM builds
    WHERE builds.image = images.id);


-- :name source-by-id :? :1
-- :doc Get source by id
SELECT * FROM sources
WHERE id = :id

-- :name all-sources :? :*
-- :doc Get images
SELECT * FROM sources;
