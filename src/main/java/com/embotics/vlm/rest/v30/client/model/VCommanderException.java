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

package com.embotics.vlm.rest.v30.client.model;

import java.io.IOException;

/**
 * Exceptions class, thrown by the vCommander plugin
 * 
 * @author btarczali
 */
public class VCommanderException extends IOException {
	private final Object[] args;
	private final String key;

	public VCommanderException(String messageKey) {
		this(messageKey, new Object[0]);
	}

	public VCommanderException(String messageKey, Object... arguments) {
		args = arguments;
		key = messageKey;
	}

	public VCommanderException(Throwable cause, String messageKey) {
		this(cause, messageKey, new Object[0]);
	}

	public VCommanderException(Throwable cause, String messageKey, Object... arguments) {
		super(cause);
		args = arguments;
		key = messageKey;
	}

	@Override
	public String getMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format(key, args));
		if (getCause() != null) {
			builder.append(" - ");
			builder.append(getCause().getMessage());
		}
		return builder.toString();
	}

	@Override
	public String getLocalizedMessage() {
		return getMessage();
	}

}
