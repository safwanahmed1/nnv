using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using ExitGames.Client.Photon;

namespace HelloWorld
{
    class Program : IPhotonPeerListener
    {

        static void Main(string[] args)
        {
            var listener = new Program();
            var peer = new PhotonPeer(listener, ConnectionProtocol.Udp);

            if (peer.Connect("localhost:5055", "Lite"))
            {
                do
                {
                    Console.WriteLine(".");
                    peer.Service();
                    System.Threading.Thread.Sleep(500);
                } while (!Console.KeyAvailable);
            }
            else
                Console.WriteLine("Unknown hostname!");
            Console.ReadKey();
        }

        public void DebugReturn(DebugLevel level, string message)
        {
            //throw new NotImplementedException();
        }

        public void OnEvent(EventData eventData)
        {
            //throw new NotImplementedException();
        }

        public void OnOperationResponse(OperationResponse operationResponse)
        {
            //throw new NotImplementedException();
        }

        public void OnStatusChanged(StatusCode statusCode)
        {
            Console.WriteLine("OnStatusChanged:" + statusCode);
        }
    }
}
