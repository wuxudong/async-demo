package main

import (
    "net/http"
    "math/rand"
    "strconv"
    "runtime"
    "time"
)

func init() {
    rand.Seed(42)
}

func RandomHandler(w http.ResponseWriter, req *http.Request) {
    time.Sleep(time.Millisecond * 200)
    w.Write([]byte(strconv.Itoa(rand.Intn(100))))
}

func AddHandler(w http.ResponseWriter, req *http.Request) {
    req.ParseForm()
    p1 := req.Form.Get("p1")
    p2 := req.Form.Get("p2")
    num1, _ := strconv.Atoi(p1)
    num2, _ := strconv.Atoi(p2)
    time.Sleep(time.Millisecond * 200)
    w.Write([]byte(strconv.Itoa(num1 + num2)))
}

func main() {
    runtime.GOMAXPROCS(runtime.NumCPU())
    http.HandleFunc("/random", RandomHandler)
    http.HandleFunc("/add", AddHandler)
    http.ListenAndServe(":8001", nil)
}
