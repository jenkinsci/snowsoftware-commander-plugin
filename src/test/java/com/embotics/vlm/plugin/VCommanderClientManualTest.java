/*************************************************************************
 *
 *	Copyright (c) 2017 Embotics Corporation. All Rights Reserved.
 *
 *	No part of this software may be reproduced, used in any
 *	information storage and retrieval system, or transmitted in
 *	any form or by any means, electronic, mechanical or otherwise,
 *	without the written permission of Embotics Corporation.
 *
 ***************************************************************************/

package com.embotics.vlm.plugin;

import com.embotics.vlm.rest.v30.client.VCommanderClient;

/**
 * Test code to try the REST API methods directly
 * 
 * @author btarczali
 *
 */
public class VCommanderClientManualTest {

	public static void main(String[] args) throws Exception {
		VCommanderClient client = new VCommanderClient("https://btarczali1d:8443", "superuser", "secret", null);

		client.getClientInfo();
		System.out.println(client.getClientInfo());
	}

}
