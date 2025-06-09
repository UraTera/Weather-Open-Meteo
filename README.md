## An example of creating a line chart library to display weather data.

The chart allows you to replace the RecyclerView adapter, which greatly simplifies the project.

The chart is configured visually during layout.

Location permission required.

Project created in Android Studio Meerkat | 2024.3.1 Patch 2.

![weather1_1](https://github.com/user-attachments/assets/1a1c4ade-f771-441d-9727-3f185fe0191c)
![weather2_3](https://github.com/user-attachments/assets/e46aa599-4cf0-4bbd-8cb9-4fd4944c8323)

To use the ready-made library, add the dependency:
```
dependencies {

    implementation("io.github.uratera:line_chart:1.0.1")
}
```

For horizontal scrolling, use HorizontalScrollView.
```
<HorizontalScrollView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:scrollbars="none">

    <com.tera.linechart.LineChart
        android:id="@+id/lineChart"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>

</HorizontalScrollView>
```
### Attributes
|Attributes              |Description            |Default value
|------------------------|-----------------------|-------------|
line_arrayColor	|Array of colors from resources |null 
line_axisColor	|Axis color             |light blue
line_axisShow	|Show axis              |true
line_axisWidth	|Axis width             |2dp
line_chartShow	|Show chart             |true
line_fillingBottomColor	|Fill color bottom      |dark gray
line_fillingTopColor	|Fill color top         |uniform
line_fillingShow	|Show fill              |false
line_fillingStyle	|Fill style (uniform, gradient) |uniform
line_iconShow	|Show icons             |true
line_iconSize	|Icon size              |12sp
line_iconTop	|Icons on top           |true
line_labelColor	|Label text color       |black
line_labelSize	|Label text size        |12sp
line_labelText	|Label text             |null
line_lineColor	|Line color             |light blue
line_lineLength	|Line length            |45dp
line_lineWidth	|Line width             |3dp
line_lineStartZero	|Start from zero        |false
line_markZeroAllHeight	|Full height zero label |false
line_markZeroColor	|Zero mark color        |light blue
line_markZeroShow	|Show zero mark         |false
line_markZeroWidth	|Zero mark width        |2dp
line_pointColor	|Points color           |light blue
line_pointRadius	|Points radius          |4dp
line_pointShow	|Show point s           |true
line_textAxisColor	|Axis text color        |black
line_textAxisShow	|Show axis text         |true
line_textAxisSize	|Axis text size         |12sp
line_textAxisTop	|Axis text top          |false
line_textColor	|Value text color       |black
line_textOnLine	|Value text on straight line |true
line_textSize	|Value text size        |12sp
line_textShow	|Show value text        |true
line_textString	|Value text with unit   |true

Methods:
```
public final var dataValueString: ArrayList<String>?
public final var dataAxisString: ArrayList<String>?
public final var icons: ArrayList<Int>?
public final var colors: ArrayList<Int>?
```
Without using the dataValueString method, the chart displays nothing.

