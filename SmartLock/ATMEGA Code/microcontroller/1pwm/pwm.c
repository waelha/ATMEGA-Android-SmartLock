#include <avr/io.h>
#include <util/delay.h>

void InitPWM()
{
	/*
	TCCR0 - Timer Counter Control Register (TIMER0)
	-----------------------------------------------
	BITS DESCRIPTION
	
	NO: 	NAME   DESCRIPTION
	--------------------------
	BIT 7 : FOC0   Force Output Compare [Not used in this example]
	BIT 6 : WGM00  Wave form generartion mode [SET to 1]
	BIT 5 : COM01  Compare Output Mode        [SET to 1]
	BIT 4 : COM00  Compare Output Mode        [SET to 0]
	BIT 3 : WGM01  Wave form generartion mode [SET to 1]
	BIT 2 : CS02   Clock Select               [SET to 0]
	BIT 1 : CS01   Clock Select               [SET to 0]
	BIT 0 : CS00   Clock Select               [SET to 1]

	The above settings are for
	--------------------------
	Timer Clock = CPU Clock (No Prescalling)
	Mode        = Fast PWM
	PWM Output  = Non Inverted

	*/


	TCCR0|=(1<<WGM00)|(1<<WGM01)|(1<<COM01)|(1<<CS00)|(1<<CS01);

	//Set OC0 PIN as output. It is  PB3 on ATmega16 ATmega32
	DDRB=0xF8;
DDRA=0x00;
}

/******************************************************************
Sets the duty cycle of output. 

Arguments
---------
duty: Between 0 - 255

0= 0%
255= 100%

The Function sets the duty cycle of pwm output generated on OC0 PIN
The average voltage on this output pin will be

         duty
 Vout=  ------ x 5v
	      255 

This can be used to control the brightness of LED or Speed of Motor.
*********************************************************************/

void SetPWMOutput(uint8_t duty)
{
	OCR0=duty;
}

//******************************************************************** 



void main()
{


	//Initialize PWM Channel 0
	InitPWM();

	//Do this forever
int a=0;
	while(1)
	{
a=PINA;

if(a==0)
SetPWMOutput(0);
else if(a==1)
SetPWMOutput(100);
else if(a==2)
SetPWMOutput(150);		
else if(a==3)
SetPWMOutput(255);


	}
}




