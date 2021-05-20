#!/usr/bin/env bash

file_passwd="$HOME/.local/share/io.parity.ethereum/password"
if [ ! -f "$file_passwd" ]; then
	echo "First time running parity"
	#Parity doesnt seem to allow unlocking of accounts on first run!
	#Need to run then kill, then run again unlocked.
	parity --chain dev &
	PID=$!
	sleep 5
	kill -9 $PID

	echo "" >"$file_passwd"
fi

# Create log file if it doesn't exist
file_log=/data/parity-logs/parity.log
if [ ! -f $file_log ]; then
	mkdir -p /data/parity-logs
	touch $file_log
fi

echo "Starting Parity ..."
parity \
	--chain dev \
	--reseal-min-period 0 \
	--jsonrpc-cors '*' \
	--jsonrpc-hosts '0.0.0.0' \
	--jsonrpc-apis 'all' \
	--geth \
	--force-ui \
	--unsafe-expose \
	--unlock "0x00a329c0648769A73afAc7F9381E08FB43dBEA72" \
	--log-file $file_log \
	--password "$file_passwd"
