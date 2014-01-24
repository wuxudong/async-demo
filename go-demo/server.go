/*
    Xudong's Async Demo in Golang
    author: Dongxu (huang@wandoujia.com)
*/
package main

import (
    "net/http"
    "strconv"
    "runtime"
    "io/ioutil"
    "time"
)

func FetchMaxHandler(w http.ResponseWriter, req *http.Request) {
    c := make(chan string)

    for i := 0; i < 10 ; i++ {
        go func(c chan string) {
            resp, _ := http.Get("http://localhost:8001/random")
            defer resp.Body.Close()
            body,_ := ioutil.ReadAll(resp.Body)
            c <- string(body)
        }(c)
    }
    ret := 0
    select {
        case num := <-c :{
            n, _ := strconv.Atoi(num)
            ret += n
        }
        case <- time.After(time.Second * 3): {
            w.Write([]byte("-1"))
            return
        }
    }
    w.Write([]byte(strconv.Itoa(ret)))
}


func FetchAddHandler(w http.ResponseWriter, req *http.Request) {
    c1 := make(chan string)
    c2 := make(chan string)

    fetch := func(c chan string) {
        resp, _ := http.Get("http://localhost:8001/random")
        defer resp.Body.Close()
        body,_ := ioutil.ReadAll(resp.Body)
        c <- string(body)
    }
    /* of course, we can use only one channel, but for demo, we use 2 channel seperately */
    go fetch(c1)
    go fetch(c2)

    num1, _ := strconv.Atoi(<-c1)
    num2, _ := strconv.Atoi(<-c2)

    ret := strconv.Itoa(num1 + num2)
    w.Write([]byte(ret))
}

func main() {
    runtime.GOMAXPROCS(runtime.NumCPU())
    http.HandleFunc("/go/add", FetchAddHandler)
    http.HandleFunc("/go/max", FetchMaxHandler)
    http.ListenAndServe(":8002", nil)
}
