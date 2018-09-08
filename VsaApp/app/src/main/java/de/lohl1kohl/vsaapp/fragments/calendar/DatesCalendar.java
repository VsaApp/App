package de.lohl1kohl.vsaapp.fragments.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class DatesCalendar extends GridView {
    public DatesCalendar(Context context) {
        super(context);
    }

    public DatesCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DatesCalendar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    }
}
