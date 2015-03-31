#include <avr/io.h>
#include <util/delay.h>
#include <inttypes.h>
#include <avr/interrupt.h>

void tempInit()
{

  ADMUX |= (1<<REFS0)|(1<<MUX0);    
    ADCSRA |= (1<<ADEN)|(1<<ADPS1)|(1<<ADPS0) 
            |(1<<ADPS2) |(1<<ADIE)|(1<<ADATE); 


}

void USARTInit(uint16_t ubrr_value)

{

   UBRRL = ubrr_value;
   UBRRH = (ubrr_value>>8); 
   UCSRC=(1<<URSEL)|(3<<UCSZ0);
   UCSRB=(1<<RXEN)|(1<<TXEN);


}

void InitPWM()
{

	TCCR0|=(1<<WGM00)|(1<<WGM01)|(1<<COM01)|(1<<CS00)|(1<<CS01);

}

char USARTReadChar()
{
	
	while(!(UCSRA & (1<<RXC)))
	{
		
	}

	return UDR;
}


void USARTWriteChar(char data)
{
	
	while(!(UCSRA & (1<<UDRE)))
	{

	}

	UDR=data;
}


/******************************************************************
         duty
 Vout=  ------ x 5v
	      255 

*********************************************************************/



void SetPWMOutput(uint8_t duty)
{
	OCR0=duty;
}

//******************************************************************** 

int b=0;

ISR(ADC_vect)	
{
    //   _delay_ms(100);                
       b=ADC;
	   b=b/2;
	    
	   //if(b%2!=0)
	   //b=b-1;
       // ADCSRA |= (1<<ADSC);     
                       
}

int main()
{

DDRB=0x08;
DDRC=0xFF;
USARTInit(12);
InitPWM();
SetPWMOutput(128);
tempInit();
sei();
 ADCSRA |= (1<<ADSC);

int data,a,d1,d2,d3,n=0;
	while(1)
	{

data=0;
data=USARTReadChar();
if(data==49)
{
a=PINB&0x03;
a=a+48;
USARTWriteChar(a);
}
else if(data==50)
SetPWMOutput(20);
else if(data==51)
SetPWMOutput(50);
else if(data==52)
SetPWMOutput(200);		
else if(data==53)
SetPWMOutput(255);
else if(data==54)
SetPWMOutput(127);
else if(data==55)
PORTC=1;
else if(data==56)
PORTC=0;
else if(data==57){
if(b<10){

USARTWriteChar(48);
USARTWriteChar(48);
USARTWriteChar(48);
USARTWriteChar(b+48);}
else if(b<100){
d1=b/10;
n=b%10;
USARTWriteChar(48);
USARTWriteChar(48);
USARTWriteChar(d1+48);
USARTWriteChar(n+48);

}
else if(b<1000){
d1=b/100;
d2=(b%100)/10;
n=(b%100)%10;
USARTWriteChar(48);
USARTWriteChar(d1+48);
USARTWriteChar(d2+48);
USARTWriteChar(n+48);

}
else if(b<1024){
d1=b/1000;
d2=(b%1000)/100;
d3=((b%1000)%100)/10;
n=((b%1000)%100)%10;
USARTWriteChar(d1+48);
USARTWriteChar(d2+48);
USARTWriteChar(d3+48);
USARTWriteChar(n+48);
}

}
	}
	return 0;
}





