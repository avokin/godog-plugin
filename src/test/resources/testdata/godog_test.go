package main

import ("./godog")

func stepDef(param int) error {return nil}

func FeatureContext(s *godog.Suite) {
    s.Step(`^my test with param (\d+)$`, stepDef)
}