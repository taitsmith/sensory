SENSORY (because it uses sensors)  

sensory uses the internal sensors to detect rotation about the x y and z axis, and uses 
[vico](https://github.com/patrykandpatrick/vico) to visualise those rotations on a chart. rotations
are also visualised by changing background colors in three separate text boxes, each representing
an axis of rotation. the app allows users to select the frequency of chart updates and stop / start
the sensors by clicking a button. starting the sensors will hide the option to update frequency and
instead display the text views visualising rotation. stopping the sensors switches back.

sensory is written (almost) entirely in jetpack compose. it also uses hilt for dependency injection
and mockito for unit tests.  


