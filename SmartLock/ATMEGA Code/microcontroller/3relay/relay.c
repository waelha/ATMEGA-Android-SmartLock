#include <avr/io.h>
#include <util/delay.h>

void main()
{

DDRA=0x00;
DDRC=0xFF;
int a=0;
	while(1)
	{
a=PINA;

if(a==0)
PORTC=0;
else
PORTC=1;


	}
}




