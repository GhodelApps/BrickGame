package com.example.alina.tetris;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.alina.tetris.figures.Figure;
import com.example.alina.tetris.figures.factory.FigureFactory;
import com.example.alina.tetris.figures.factory.FigureType;
import com.example.alina.tetris.listeners.OnFigureStoppedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alina on 18.03.2017.
 */

public class PlayingArea extends View implements OnFigureStoppedListener {

    private int widthOfSquareSide;

    private int squareCount;

    private int screenHeight;

    private int screenWidth;

    private int scale;

    private final int SQUARE_COUNT_VERTICAL = 10;

    private final int FIGURE_STOPPED_SCORE = 10;

    private final float LINE_WIDTH = 1f;

    private final List<FigureType> figureTypeList = new ArrayList<>();

    private final List<Figure> figureList = new ArrayList<>();

    private Paint paint;

    private NetManager netManager;

    private FigureCreator figureCreator;

    public TextView score;

    public PlayingArea(@NonNull Context context) {
        super(context);
        init();
    }

    public PlayingArea(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayingArea(@NonNull Context context, @Nullable AttributeSet attrs,
                       @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        figureCreator = new FigureCreator();
    }

    private void drawHorizontalLines(Canvas canvas) {
        for (int i = 1; i <= squareCount; i++) {
            canvas.drawLine(0, screenHeight - widthOfSquareSide * i, screenWidth,
                    screenHeight - widthOfSquareSide * i, paint);
        }
    }

    private void drawVerticalLines(Canvas canvas) {
        for (int i = 1; i <= SQUARE_COUNT_VERTICAL; i++) {
            canvas.drawLine(i * widthOfSquareSide, 0, i * widthOfSquareSide,
                    screenHeight, paint);
        }
    }

    private void startMoveDown() {
        netManager.printNet();
        new CountDownTimer(4000, 2000) {
            public void onTick(long millisUntilFinished) {

            }
            public void onFinish() {
                if (netManager.isNetFreeToMoveDown()) {
                    figureList.get(figureList.size() - 1).moveDown();
                    netManager.moveDownInNet();
                    invalidate();
                } else {
                    netManager.changeFigureStatus();
                }
            }
        }.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(LINE_WIDTH);
        drawVerticalLines(canvas);
        drawHorizontalLines(canvas);
        for(Figure figure: figureList) {
            if (figure != null) {
                Path path = figure.getPath();
                paint.setColor(figure.getColor());
                canvas.drawPath(path, paint);
                if(figure.status == Status.MOVING) {
                    startMoveDown();
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        widthOfSquareSide = MeasureSpec.getSize(widthMeasureSpec) / SQUARE_COUNT_VERTICAL;
        squareCount = MeasureSpec.getSize(heightMeasureSpec) / widthOfSquareSide;
        scale = widthOfSquareSide - (MeasureSpec.getSize(heightMeasureSpec) % widthOfSquareSide);
        screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        screenWidth = MeasureSpec.getSize(widthMeasureSpec);
    }

    public void moveLeft() {
        if (netManager.isNetFreeToMoveLeft()) {
            figureList.get(figureList.size() - 1).moveLeft();
            netManager.moveLeftInNet();
            netManager.printNet();
            invalidate();
        }
    }

    public void moveRight() {
        if (netManager.isNetFreeToMoveRight()) {
            figureList.get(figureList.size() - 1).moveRight();
            netManager.moveRightInNet();
            netManager.printNet();
            invalidate();
        }
    }

    public void addFigure(FigureType figureType) {
        figureTypeList.add(figureType);
        Figure figure = FigureFactory.getFigure(figureTypeList.get(figureTypeList.size() - 1),
                widthOfSquareSide, scale, getContext());
        figureList.add(figure);
        if (figure != null) {
            figure.initFigureMask();
        }
        if (netManager == null) {
            netManager = new NetManager();
            netManager.initNet(squareCount, SQUARE_COUNT_VERTICAL);
        }
        netManager.initFigure(figure);
        netManager.setOnFigureStoppedListener(this);
        netManager.printNet();
        invalidate();
    }

    @Override
    public void onFigureStoppedMove() {
        addFigure(figureCreator.selectFigure());
    }

    private void setScore(int value) {
        int scoreValue = Integer.parseInt(score.getText().toString());
        scoreValue += value;
        score.setText(scoreValue);
    }
}
