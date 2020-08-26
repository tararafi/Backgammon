/*
 * [board.java]
 * This file contains the class for the backgammon board, which includes piece location, moving, and dice rolling. 
 * Authour: Tara Rafi
 */
 

package Backgammon;

import java.util.Random;

//Main Class
public class board
{
        int count[];
        int type[];
        
//Count for black and white checkers
        static final int neutral = 0;
        static final int white = 1;
        static final int black = 2;

//If dice is rolled
        boolean rolled = false;

//Gives value for dice and black/white bars
        Random rdice;
        int dice1;
        int dice2;
        int white_bar = 0;
        int black_bar = 0;
        int white_bear = 0;
        int black_bear = 0;

//Creating board method to set columns 
        public board()
        {
                count = new int[25];
                type = new int[25];
                rdice = new Random();

                for(int i=0;i<25;i++)
                {
                        count[i] = 0;
                        type[i] = neutral;
                }

                setColumn(1, 2, white);
                setColumn(6, 5, black);
                setColumn(8, 3, black);
                setColumn(12, 5, white);
                setColumn(13, 5, black);
                setColumn(17, 3, white);
                setColumn(19, 5, white);
                setColumn(24, 2, black);
        }

        
        public int getColor(int col)
        {
                return type[col];
        }

        public int getCount(int col)
        {
                return count[col];
        }


        public void setColumn(int col, int num, int clr)
        {
                count[col] = num;
                if(num==0)
                        clr=neutral;
                type[col] = clr;

        }
//Rolling dice and getting values from each one
        public void rollDice()
        {
                dice1 = rdice.nextInt(6) + 1;
                dice2 = rdice.nextInt(6) + 1;
                rolled = true;
        }

        public int getDice1()
        {
                return dice1;
        }

        public int getDice2()
        {
                return dice2;
        }

        public void resetDice()
        {
                dice1 = 0;
                dice2 = 0;
                rolled = false;
        }

        public void moveToBar(int spike)
        {
                if(getColor(spike)==white)
                        white_bar++;
                else
                        black_bar++;
                setColumn(spike, 0, neutral);
        }
//Getting count for white and black checkers
        public int getBlack()
        {
                int sum = 0;

                for(int i=1;i<25;i++)
                {
                        if(getColor(i)==black)
                                sum += getCount(i);
                }

                return sum;
        }

        public int getWhite()
        {
                int sum = 0;

                for(int i=1;i<25;i++)
                {
                        if(getColor(i)==white)
                                sum += getCount(i);
                }

                return sum;
        }

//If white or black bearings can be beared off
        public boolean canBearOff(int color)
        {
                int sum = 0;

                if(color==white)
                {
                        for(int i=19;i<=24;i++)
                        {
                                if(getColor(i)==white)
                                        sum += getCount(i);
                        }
                        sum += white_bear;
                }

                if(color==black)
                {
                        for(int i=1;i<=6;i++)
                        {
                                if(getColor(i)==black)
                                        sum += getCount(i);
                        }
                        sum += black_bear;
                }

                if(sum==15)
                        return true;         //There are 15 pieces in backgammon

                return false;
        }

        public boolean onBar(int color)
        {
                if(color==white)
                {
                        if(white_bar>0)
                                return true;
                        return false;
                }

                if(color==black)
                {
                        if(black_bar>0)
                                return true;
                        return false;
                }

                return false;
        }
//Resetting dice
        public void setDice(int roll1, int roll2)
        {
                dice1 = roll1;
                dice2 = roll2;
        }
}
