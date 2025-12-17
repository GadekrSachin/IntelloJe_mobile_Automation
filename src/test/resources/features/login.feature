Feature: Login

  Scenario: Successful navigation to 3D Transition
    Given the app is launched
    When I enter username "testuser" and password "Password123"
    And I tap the login button
    Then I should see the home screen

