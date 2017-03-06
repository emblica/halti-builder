(ns halti-builder.db.builds
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "halti_builder/db/sql/builds.sql")
