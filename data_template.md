## Map data.txt file structure & example

```json5
{
  "name": "Scandinavia",
  
  "cities": [
    "Stockholm",
    "Gothenburg"
  ],
  "train_size": [10, 20], // The width and height of a train
  "billboards": [ // The coordinates for all the billboards, last is yaw
    [-11, 102, -11, -90],
    [-5, 102, -17, 180],
    [4, 102, -17, 180],
    [10, 102, -11, 90]
  ],
  "tracks": [
    {
      "point_a": "Stockholm",
      "point_b": "Gothenburg",
      "subtracks": [ // Each destination can be made out of multiple subtracks
        {
          "type": 0, // The color, see data types further down
          "parts": [ // Each track should have between 1 and 6 parts
            {
              "pos": [0, 0], // The top left position of the track
              "rotation": 0 // 0: Horizontal, 1: Vertical, 2: Diagonal POS, 3: Diagonal NEG
            }
          ]
        }
      ]
    }
  ],
  "map_cards": [ // A list of the available destination cards
    {
      "point_a": "Stockholm",
      "point_b": "Gothenburg",
      "reward": 10, // points
      "image": "/maps/sto_got.png" // A relative path to the map card image
    }
  ]
}
```

### Track types:

| Track Type | Color           |
|------------|-----------------|
| 0          | Gray (any type) |
| 1          | Yellow          |
| 2          | Orange          |
| 3          | Red             |
| 4          | White           |
| 5          | Green           |
| 6          | Purple          |
| 7          | Gray            |