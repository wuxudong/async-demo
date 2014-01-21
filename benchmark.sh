#! /bin/bash

#siege is needed
siege -t 10S -c 100 "http://127.0.0.1:9888/blocking/morra?uids=1,2,3,4,5,6,7,8,9,10"
siege -t 10S -c 100 "http://127.0.0.1:9888/async/morra?uids=1,2,3,4,5,6,7,8,9,10"
siege -t 10S -c 100 "http://127.0.0.1:9888/scala/morra?uids=1,2,3,4,5,6,7,8,9,10"
siege -t 10S -c 100 "http://127.0.0.1:9888/callable/morra?uids=1,2,3,4,5,6,7,8,9,10"
siege -t 10S -c 100 "http://127.0.0.1:9888/deferred/morra?uids=1,2,3,4,5,6,7,8,9,10"
siege -t 10S -c 100 "http://127.0.0.1:9999/morra-demo?uids=1,2,3,4,5,6,7,8,9,10"

siege -t 10S -c 100 "http://127.0.0.1:9888/blocking/sum"
siege -t 10S -c 100 "http://127.0.0.1:9888/async/sum"
siege -t 10S -c 100 "http://127.0.0.1:9888/scala/sum"
siege -t 10S -c 100 "http://127.0.0.1:9888/callable/sum"
siege -t 10S -c 100 "http://127.0.0.1:9888/deferred/sum"
siege -t 10S -c 100 "http://127.0.0.1:9999/sum-demo"

