package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class TicketServiceImpl implements TicketService {

    private static int CHILD_TICKET_PRICE = 10;
    private static int ADULT_TICKET_PRICE =20;

    private static final Logger LOGGER = Logger.getLogger(TicketServiceImpl.class.getName());
    private TicketPaymentService ticketPaymentService;

    private SeatReservationService seatReservationService;

    public TicketServiceImpl ( final TicketPaymentService ticketPaymentService,
                              final SeatReservationService seatReservationService )
    {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

      // check if total number of tickets is less than 20 and there are always adult ticket per infant ticket and there is adult ticket for child ticket booking
        if(validateNumOfTickets( ticketTypeRequests) && validateChildTickets(ticketTypeRequests) && validateInfantTickets(ticketTypeRequests))
        {

                final int  totalPayment =  getTotalPaymentAmount(ticketTypeRequests);
                ticketPaymentService.makePayment(accountId, totalPayment);
                seatReservationService.reserveSeat(accountId, getTotalNumberOfSeatsToReserve(ticketTypeRequests));
        }

       else {
           throw new InvalidPurchaseException();
        }
    }
    // validate if number of tickets are less than 20
    boolean validateNumOfTickets (final TicketTypeRequest... ticketTypeRequests)
    {
        final int numberOfRequestedTickets = Arrays
                .stream(ticketTypeRequests)
                .mapToInt(TicketTypeRequest ::getNoOfTickets)
                .reduce(0, (value1, value2) -> value1 + value2);
        return numberOfRequestedTickets > 20 ? false : true;
    }

    // count number of adult tickets
    int numberOfAdultTickets(final TicketTypeRequest... ticketTypeRequests )
    {
        int numberOfAdultTickets = getTicketsByType(ticketTypeRequests).get(TicketTypeRequest.Type.ADULT) != null ? getTicketsByType(ticketTypeRequests).get(TicketTypeRequest.Type.ADULT).intValue() : 0;
        return numberOfAdultTickets;
    }

    //count number of child tickets
    int numberOfChildTickets(final TicketTypeRequest... ticketTypeRequests)
    {
        int numberOfChildTickets=  getTicketsByType(ticketTypeRequests).get(TicketTypeRequest.Type.CHILD) !=null ? getTicketsByType(ticketTypeRequests).get(TicketTypeRequest.Type.CHILD).intValue() :0;
        return numberOfChildTickets;
    }

    //count number of infant tickets
    int numberOfInfantTickets(final TicketTypeRequest... ticketTypeRequests)
    {
        int numberOfInfantTickets =  getTicketsByType(ticketTypeRequests).get(TicketTypeRequest.Type.INFANT)!= null ? getTicketsByType(ticketTypeRequests).get(TicketTypeRequest.Type.INFANT).intValue() : 0;
     return numberOfInfantTickets;
    }
    //validate if there is associated adult ticket booked for child ticket booking
    boolean validateChildTickets(final TicketTypeRequest... ticketTypeRequests)
    {
       return numberOfChildTickets(ticketTypeRequests) <= numberOfAdultTickets(ticketTypeRequests);
    }

    // validate there is always adult ticket booked for each infant ticket booking
    private boolean validateInfantTickets(final TicketTypeRequest... ticketTypeRequests)
    {
       return numberOfInfantTickets(ticketTypeRequests) <= numberOfAdultTickets(ticketTypeRequests) ;
    }

    // returns number of tickets for each ticket type
     Map<TicketTypeRequest.Type, Integer> getTicketsByType( final TicketTypeRequest... ticketTypeRequests)
    {
        Map<TicketTypeRequest.Type, Integer> ticketsByType = Arrays.stream(ticketTypeRequests).collect(Collectors.groupingBy(TicketTypeRequest::getTicketType, Collectors.summingInt(TicketTypeRequest::getNoOfTickets)));
        return ticketsByType;
    }

    //calculate total number of tickets to be booked (child + adult)
    int getTotalNumberOfSeatsToReserve( TicketTypeRequest... ticketTypeRequests )
    {
        final int numberOfChildTickets =  numberOfChildTickets( ticketTypeRequests);
        final int numberOfAdultTickets = numberOfAdultTickets(ticketTypeRequests);
        return numberOfAdultTickets + numberOfChildTickets;
    }

    //calculate total amount for payment
    int getTotalPaymentAmount (final TicketTypeRequest... ticketTypeRequests)
    {
        return numberOfChildTickets(ticketTypeRequests) * CHILD_TICKET_PRICE + numberOfAdultTickets(ticketTypeRequests) * ADULT_TICKET_PRICE;
    }
}
