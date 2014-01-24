package main

import (
    "net/http"
    "math/rand"
    "strconv"
    "runtime"
)

func init() {
    rand.Seed(42)
}

func RandomHandler(w http.ResponseWriter, req *http.Request) {
    w.Write([]byte(strconv.Itoa(rand.Intn(100))))
}

func main() {
    runtime.GOMAXPROCS(runtime.NumCPU())
    http.HandleFunc("/random", RandomHandler)
    http.ListenAndServe(":8001", nil)
}
