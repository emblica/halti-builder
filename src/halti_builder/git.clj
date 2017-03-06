(ns halti-builder.git
  (:require [clj-jgit.porcelain :refer :all]
            [clj-jgit.querying :refer :all]
            [halti-builder.utils :refer [uuid]]))



(defn last-commit [repo]
  (let [raw-commit (first (git-log repo))
        all-commit-data (commit-info repo raw-commit)]
    (select-keys all-commit-data [:id :author :email :message :time])))


(defn update-repo [address directory config]
  (with-identity config
    (let [repo (try (do (load-repo directory))
                    (catch Exception e (:repo (git-clone-full address directory))))]
      (do
        (git-fetch repo)
        (git-pull repo)
        (last-commit repo)))))
