package project2.madcamp02;

/**
 * Created by q on 2016-12-27.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class squareimageview extends ImageView
{


    public squareimageview(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public squareimageview(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }


    public squareimageview(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(),getMeasuredWidth());

    }

}
