(ns halti-builder.db.registries
  (:require [hugsql.core :as hugsql]))


(hugsql/def-db-fns "halti_builder/db/sql/registries.sql")
