using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using ExitGames.Client.Photon;
using ExitGames.Client.Photon.Lite;
using System.Collections;

namespace HelloWorld
{
    class Program : IPhotonPeerListener
    {
        public PhotonPeer peer;

        enum OpCodeEnum : byte
        {
            Join = 255,
            Leave = 254,
            RaiseEvent = 253,
            SetProperties = 252,
            GetProperties = 251
        }

        enum EvCodeEnum : byte
        {
            Join = 255,
            Leave = 254,
            PropertiesChanged = 253
        }

        static void Main(string[] args)
        {
            new Program().Run();
        }

        public Program()
        {
            peer = new PhotonPeer(this, ConnectionProtocol.Udp);
        }

        public void Run()
        {

            if (peer.Connect("localhost:5055", "Lite"))
            {
                do
                {
                    //Console.WriteLine(".");
                    peer.Service();
                    System.Threading.Thread.Sleep(500);
                } while (!Console.KeyAvailable);
            }
            else
                Console.WriteLine("Unknown hostname!");
            Console.WriteLine("Press any key to end program!");
            Console.ReadKey();
        }

        public void DebugReturn(DebugLevel level, string message)
        {
            //throw new NotImplementedException();
        }

        public void OnEvent(EventData eventData)
        {
            Console.WriteLine("\n---OnEvent: " + (EvCodeEnum)eventData.Code + "(" + eventData.Code + ")");

            switch (eventData.Code)
            {
                case 101:
                    int sourceActorNr = (int)eventData.Parameters[LiteEventKey.ActorNr];
                    Hashtable evData = (Hashtable)eventData.Parameters[LiteEventKey.Data];
                    Console.WriteLine(" ->Player" + sourceActorNr + " say's: " + evData[(byte)1]);
                    break;
            }
        }

        public void OnOperationResponse(OperationResponse operationResponse)
        {
            // check return code = 0 is ok, else is not ok and exit
            if (operationResponse.ReturnCode == 0)
            {
                Console.WriteLine("\n---OnOperationResponse: OK - " + (OpCodeEnum)operationResponse.OperationCode + "(" + operationResponse.OperationCode + ")");
            }
            else
            {
                Console.WriteLine("\n---OnOperationResponse: NOK - " + (OpCodeEnum)operationResponse.OperationCode + "(" + operationResponse.OperationCode + ")\n ->ReturnCode=" + operationResponse.ReturnCode + " DebugMessage=" + operationResponse.DebugMessage);
                return;
            }

            // 
            switch (operationResponse.OperationCode)
            {
                case (byte)LiteOpCode.Join:
                    int myActorNr = (int)operationResponse.Parameters[LiteOpKey.ActorNr];
                    Console.WriteLine(" ->My PlayerNr (or ActorNr) is:" + myActorNr);
 
                    Console.WriteLine("Calling OpRaiseEvent ...");
                    Dictionary<byte, object> opParams = new Dictionary<byte, object>();
                    opParams[LiteOpKey.Code] = (byte)101;
                    //opParams[LiteOpKey.Data] = "Hello World!";
                    Hashtable evData = new Hashtable();
                    evData[(byte)1] = "Hello Wolrd!";
                    opParams[LiteOpKey.Data] = evData;
                    peer.OpCustom((byte)LiteOpCode.RaiseEvent, opParams, true);
            
                    break;
            }
        }

        public void OnStatusChanged(StatusCode statusCode)
        {
            Console.WriteLine("OnStatusChanged:" + statusCode);
            switch (statusCode)
            {
                case StatusCode.Connect:
                    Console.WriteLine("calling OpJoin");
                    Dictionary<Byte, Object> opParams = new Dictionary<Byte, Object>();
                    opParams[(byte)LiteOpKey.GameId] = "MyRoomName";
                    peer.OpCustom((byte)LiteOpCode.Join, opParams, true);
                    break;
                default: break;
            }
        }



        // end
    }
}
