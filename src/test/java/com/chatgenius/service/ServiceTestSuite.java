package com.chatgenius.service;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    UserServiceTest.class,
    ChatServiceTest.class,
    MessageServiceTest.class
})
public class ServiceTestSuite {
    // This class serves as a test suite container
} 