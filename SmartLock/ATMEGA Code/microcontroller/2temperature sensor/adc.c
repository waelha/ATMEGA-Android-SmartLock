/*******************************************************************************
*
*   USING THE ADC WITH CONVERSION COMPLETE-INERRUPT IN SINGLE CONVERSION MODE
*   -------------------------------------------------------------------------
*
*   Filename:                                    
*   Autor:                  David Kress
*
*   Version:                1.0
*   Date:                   20. March 2011
*   Built with:             ATMEL AVR Studio 4.14 and AVR-GCC; Optimization -01
*
*   Description:            This programm is a simple example hwo to use the
*                           ATMEGA16 ADC in single conversion mode with
*                           interrupt at the end of the conversion. While
*                           measuring the analog voltage, the CPU is busy
*                           with toggeling the output PB1. AVCC is used
*                           as referencevoltage and ADC4 is selected by the
*                           mux. The voltage is measured single ended. The
*                           output of the conversion is compared with 512 and 
*                           PB0 shows the relust.
*                           With an oscilloscope, you can observe the duration
*                           of a conversion on PB2.
*                           
*   What you can learn:     - Select AVCC as ADC voltagereference
*                           - Enable the ADC interrupt
*                           - Write the aDC interrupt routine
*                           - Select an ADC clockprescaler
*                           - Select an ADC input with the ADC MUX 
*                           - Enable the ADC
*                           - Start a single Conversion
*                           - Read and use the result in the interruptroutine
*
*   Clock source:           - Any 1MHz clock
*
*   Recommended             - High: 0x99
*   Fuse settings:          - Low:  0xE1
*
*
*
*   Schematic:       ___________________
*                   |      ATMEGA16     |
*          Vcc ---->|Vcc                |
*          Vcc ---->|RESET              |
*                   |              AREF |----
*          GND ---->|GND                |   |
*                   |                   |  ===
*          Vcc      |                   |   |
*           |       |                   |  GND
*           |       |                   |             
*          |P|      |                   |
*     10k  |O|      |                   |
*          |T|----->| PA4 (ADC4)        |             
*          |I|      |               PB0 |----> DOUT
*           |       |               PB1 |----> DOUT
*           |       |               PB2 |----> DOUT     
*          GND      |                   |
*
*******************************************************************************/

#include <util/delay.h>
#include <avr/io.h>
#include <avr/interrupt.h>

int main(void)
{

    DDRB = 0xff;                      // Setup PB0, PB1 and PB2 as output
//DDRA = 0x00;
    
    ADMUX |= (1<<REFS0);    // Set Reference to AVCC and input to ADC4
    ADCSRA |= (1<<ADEN)|(1<<ADPS1)    // Enable ADC, set prescaler to 16
            |(1<<ADPS2) |(1<<ADIE);              // Fadc=Fcpu/prescaler=1000000/16=62.5kHz
                                      // Fadc should be between 50kHz and 200kHz
      	                                // Enable ADC conversion complete interrupt

    sei();                            // Set the I-bit in SREG

    ADCSRA |= (1<<ADSC);              // Start the first conversion
                      // Indicate the start of the conversion
  
  
    for(;;)                           // Endless loop;
    {
                       // Toggle PB1

    }                                 // main() will never be left

    return 0;                         // This line will never be executed

}


// Interrupt subroutine for ADC conversion complete interrupt
ISR(ADC_vect)	
{
                      // Indicate the end of the conversion
        PORTB=ADCL;        
		_delay_ms(1000);
        ADCSRA |= (1<<ADSC);          // Start the next conversion
                        // Indicate the start of the conversion
}
