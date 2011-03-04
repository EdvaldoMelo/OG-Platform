/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_stdafx_h
#define __inc_og_language_connector_stdafx_h

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include <ShellAPI.h>
#include <tchar.h>
#include <strsafe.h>
#ifdef __cplusplus
#pragma warning(disable:4995) /* suppress #pragma deprecated warnings from standard C++ headers */
#endif /* ifdef __cplusplus */
#else
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#endif

#include <assert.h>

#include <Util/Fudge.h>
#ifdef __cplusplus
#include <Util/BufferedInput.h>
#include <Util/File.h>
#include <Util/Logging.h>
#include <Util/Mutex.h>
#include <Util/NamedPipe.h>
#include <Util/Process.h>
#include <Util/Semaphore.h>
#include <Util/String.h>
#include <Util/Thread.h>
#endif /* ifdef __cplusplus */

#endif /* ifndef __inc_og_language_connector_stdafx_h */
