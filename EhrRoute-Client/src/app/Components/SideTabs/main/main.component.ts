import { AuthService } from 'src/app/Services/auth.service';
import { AddressService } from './../../../Services/address.service';
import { Component, OnInit } from '@angular/core';
import { NodeNetworkService } from 'src/app/Services/node-network.service';
import { MainLayoutService } from './../../../Services/main-layout.service';
import { NodeClustersService } from 'src/app/Services/node-clusters.service';


@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.css']
})


export class MainComponent implements OnInit
{
   
   constructor(
      public mainLayout:MainLayoutService, private clustersService:NodeClustersService, 
      private networkService:NodeNetworkService, private addressService:AddressService,
      private authService:AuthService
   ) {
      this.mainLayout.show();
   }


   ngOnInit() 
   {
      // Handles when user reloads page after loggin in, to show a prompt, which 
      // allows for a request to be made, unsubscribing the node from clusters.
      //this.handleReloads();

      // If user has provider role
      if (this.authService.isUserProvider()) {

         // Establish connection to user's address DB
         this.addressService.ensureAddressDBsConnection();

         // Establish connections to all of user's networks DBs if they exist
         this.networkService.checkUserNetworks();

      }
   }


   handleReloads(): void
   {
      var showMsgTimer;
      var clusterService = this.clustersService;

      // Before user refreshes page, show a prompt asking for confirmation
      window.onbeforeunload = function(evt) {       
         
         // Unsubscribe from clusters and close SSE http connections (EventSource connections)
         clusterService.unsubscribeClusters();

         showMsgTimer = window.setTimeout(showMessage, 500);
 
         evt.returnValue = '';
     
         return '';
      }
     
      window.onunload = function () {
         clearTimeout(showMsgTimer);
      }

      // If user decides to stay on page
      function showMessage() {
         // Subscribe to clusters again
         clusterService.subscribeClusters();
      }
   }

}
