import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {

    private TicketServiceImpl ticketServiceImpl;
     private SeatReservationService seatReservationServiceMock;

     private TicketPaymentService ticketPaymentServiceMock;


    @Before
    public void init()
    {
        seatReservationServiceMock =mock(SeatReservationServiceImpl.class);
        ticketPaymentServiceMock = mock(TicketPaymentServiceImpl.class);
        ticketServiceImpl = new TicketServiceImpl(ticketPaymentServiceMock, seatReservationServiceMock);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testIfNumberOfTicketsGreaterThanTwentyThrowException()
    {
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,15);
        TicketTypeRequest infantTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 6);
        TicketTypeRequest ticketTypeRequests[] = {adultTicketRequest, infantTicketRequest};
        ticketServiceImpl.purchaseTickets(45L, ticketTypeRequests);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testIfNumberOfInfantTicketsGreaterThanAdultTicketsThrowsException()
    {
        TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,5);
        TicketTypeRequest infantTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 6);
        TicketTypeRequest childTicketrequest =  new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest ticketTypeRequests[] = {adultTicketRequest, infantTicketRequest};
        ticketServiceImpl.purchaseTickets(45L, ticketTypeRequests);

    }
    @Test(expected = InvalidPurchaseException.class)
    public void testIfPurchasingChildTiecketWithoutAdultThrowsException( )
    {
        TicketTypeRequest childTicketrequest =  new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest ticketTypeRequests[] = {childTicketrequest};
        ticketServiceImpl.purchaseTickets(45L, ticketTypeRequests);
    }

}
