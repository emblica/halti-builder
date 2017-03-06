-- :name create-builds-table
-- :command :execute
-- :result :raw
-- :doc Create builds table
CREATE TABLE builds
(
    id serial PRIMARY KEY,
    image INTEGER,
    commit_hash VARCHAR(400),
    commit_time TIMESTAMP WITH TIME ZONE,
    commit_author VARCHAR(400),
    commit_message VARCHAR(400),
    author VARCHAR(400) DEFAULT 'SYSTEM',
    tag VARCHAR(400),
    branch VARCHAR(400),
    status VARCHAR(40) DEFAULT 'PENDING'::character varying,
    status_message VARCHAR(400) DEFAULT ''::character varying,
    started TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    ready TIMESTAMP WITH TIME ZONE
);

-- :name create-logs-table
-- :command :execute
-- :result :raw
-- :doc Create log table
CREATE TABLE logs
(
    id serial PRIMARY KEY NOT NULL,
    ts TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    build INTEGER NOT NULL,
    phase VARCHAR(120),
    message TEXT
);

-- :name insert-build :<! :1
-- :doc Insert a single log event returning affected row count
insert into builds (image, author, tag, branch)
values (:image, :author, :tag, :branch)
returning id;

-- :name update-build-status :! :1
-- :doc Update build status
update builds set status = :status, status_message = :status-msg where id = :id;

-- :name update-build-commit-data :! :1
-- :doc Update build commit data
update builds
  set commit_hash = :hash,
      commit_message = :message,
      commit_author = :author,
      commit_time = :time
  where id = :id;

-- :name complete-build :! :1
-- :doc Update build status
update builds set status = 'DONE', status_message = 'Build complete', ready = now() where id = :id;

-- :name insert-event :<! :1
-- :doc Insert a single log event returning affected row count
insert into logs (build, phase, message)
values (:build, :phase, :message)
returning id;



-- :name build-by-id :? :1
-- :doc Get build by id
select * from builds
where id = :id


-- :name all-builds :? :*
-- :doc Get all builds
select * from builds

-- :name logs-by-build-id :? :*
-- :doc Get logs by build id
select * from logs
where build = :id
