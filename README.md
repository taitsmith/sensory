SENSORY (because it uses sensors)  

sensory uses the internal sensors to detect rotation about the x y and z axis, and uses 
[vico](https://github.com/patrykandpatrick/vico) to visualise those rotations on a chart. rotations
are also visualised by changing background colors in three separate text boxes, each representing
an axis of rotation. the app allows users to select the frequency of chart updates and stop / start
the sensors by clicking a button. starting the sensors will hide the option to update frequency and
instead display the text views visualising rotation. stopping the sensors switches back.

sensory is written (almost) entirely in jetpack compose. it also uses hilt for dependency injection
and mockito for unit tests.  

a few notes on design choices, etc:  
the app is locked to landscape mode to make the chart easier to display. the chart autoscrolls to 
follow new updates, but stops after 25 ticks. this seems to be the default behavior of the library.  
the legend is fairly simple and doesn't use vico's built-in legend function, which currently only
provides support for displaying a vertically-oriented legend below the chart, forcing everything
else off screen.  
i'd normally use hilt to inject a sensor repository to the viewmodel, but doing that then causes
issues with mockito and junit when mocking the view model for testing, which in turn causes all
tests to fail / not run.

