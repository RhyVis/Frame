# Frame - Text Game Engine

The project is currently primitive, in early stage development.
The goal is to create a game engine that can be used to create text-based games.

Currently, there are several features that are implemented:

### FRC - Frame Compose
A simple file structure to store lines of text that can be used to create a story.
Can be referenced by the game engine to display text to the player.

```
$name // Name of the compose

[Paragraph]

... // Text content, until the next paragraph declaration

[Another Paragraph]

... // Contents can be multiple lines, ignoring empty lines

``  // Declare an anomyous paragraph

... // Anonymous have name created by the engine, and can be referenced by the index

// EOF regarded also the end of the last paragraph
```

### FRG - Frame Graph
A simple file structure to store the flow of the game. 
Like scripts, have a list of actions that can be executed by the engine.

After the first run, the structure of FRG will be stored in FRB file. 
Next time the game is run, if FRG File is not modified,
the FRB file will be used instead of FRG, to speed up the process by skipping parsing process.

The content here is just part of the use, may refer to docs and antlr grammar for more information.

```
$name // Name of the graph

$[Reference]              // Reference to a FRC file
$[Reference][0]           // Reference to a FRC file, with a specific paragraph index
$[Reference]["Paragraph"] // Reference to a FRC file, with a specific paragraph name

@function_call() {}       // Define a function

function_call()           // Call a function

#println("Hello, World!") // Call system function to directly interact with engine
```
