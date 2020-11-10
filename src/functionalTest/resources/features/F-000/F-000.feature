@F-000
Feature: F-000 : Case worker health check

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-000
  Scenario: must see Case worker health is UP
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Case worker API],
    And it is submitted to call the [Case worker health API] operation of [Case worker API],
    Then a positive response is received,
    And the response has all other details as expected.

