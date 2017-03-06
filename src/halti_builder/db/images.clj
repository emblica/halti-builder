(ns halti-builder.db.images
  (:require [hugsql.core :as hugsql]))


(hugsql/def-db-fns "halti_builder/db/sql/images.sql")
